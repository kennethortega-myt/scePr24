package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import pe.gob.onpe.sceorcbackend.model.dto.TransmisionCreated;
import pe.gob.onpe.sceorcbackend.model.dto.transmision.TransmisionResponseDto;
import pe.gob.onpe.sceorcbackend.model.enums.TransmisionNacionEnum;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Acta;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ActaTransmisionNacion;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.CentroComputo;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ProcesoElectoral;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionHttpService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.CentroComputoService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.MaeProcesoElectoralService;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.TransmisionReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.TransmisionRequestDto;
import pe.gob.onpe.sceorcbackend.utils.ConstantesComunes;
import pe.gob.onpe.sceorcbackend.utils.SceConstantes;


@Service
public class ActaTransmisionNacionHttpServiceImpl implements ActaTransmisionNacionHttpService {

	Logger logger = LoggerFactory.getLogger(ActaTransmisionNacionHttpServiceImpl.class);

	public static final String URL_NACION_RECIBIR_TRANSMISION = "transmision/recibir-transmision/";

	@Value("${sce.nacion.url}")
	private String urlNacion;

	@Value("${app.orc.transmision.cantidad-bloques-procesamiento}") // sacar esto
	private Integer cantidadBloqueProcesamiento;

	private final ActaTransmisionNacionService actaTransmisionNacionService;

	private final MaeProcesoElectoralService procesoElectoralService;
	
	private final CentroComputoService centroComputoService;

	private final RestTemplate clientExport;

	public ActaTransmisionNacionHttpServiceImpl(ActaTransmisionNacionService actaTransmisionNacionService,
			RestTemplate clientExport,
			MaeProcesoElectoralService procesoElectoralService,
			CentroComputoService centroComputoService) {
		this.clientExport = clientExport;
		this.actaTransmisionNacionService = actaTransmisionNacionService;
		this.procesoElectoralService = procesoElectoralService;
		this.centroComputoService = centroComputoService;
	}

	@Override
	@Transactional
	@Async
	public void sincronizar(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario) {
	 TransmisionCreated saved = null;
	 try {	
		boolean pendientes = this.actaTransmisionNacionService.hayPendientes(idActa);
		if(!pendientes){ // no hay pendientes
			saved = this.actaTransmisionNacionService.guardarTransmision(
					idActa, 
					estadoEnum, 
					usuario, 
					proceso, 
					ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE,
					ConstantesComunes.CERO_INTENTO_TRANSMISION);
			if (saved.isSuccess() && proceso != null) {
				this.tramsmitirAsync(saved.getIdTransmision(), proceso, usuario);
			} else {
				this.actaTransmisionNacionService.actualizarEstado(
						saved.getIdTransmision(), 
						ConstantesComunes.ESTADO_TRANSMISION_ERROR);
				logger.info("No se realiza la sincronizacion");
			}
		} else {
			this.actaTransmisionNacionService.guardarTransmision(
					idActa, 
					estadoEnum, 
					usuario, 
					proceso, 
					ConstantesComunes.ESTADO_TRANSMISION_ERROR,
					ConstantesComunes.CERO_INTENTO_TRANSMISION);
		}
		
	 } catch (Exception ex) {
		 // cambiar de estado por error
		 if(saved!=null){
			 this.actaTransmisionNacionService.actualizarEstado(
					 	saved.getIdTransmision(), 
						ConstantesComunes.ESTADO_TRANSMISION_ERROR);
			 logger.error("Error inesperado durante sincronización. ActaID={}", idActa, ex);
		 }
	 }
	}
	
	@Override
	@Transactional
	@Async
	public void sincronizar(List<Long> idActas, String proceso, TransmisionNacionEnum estadoEnum, String usuario) {
		logger.info("Se inicio el proceso de registrar la transmision: ");
		logger.info("proceso: {}", proceso);
		logger.info("estado: {}", estadoEnum.name());
		for(Long idActa:idActas){
			boolean pendientes = this.actaTransmisionNacionService.hayPendientes(idActa);
			TransmisionCreated saved = null;
			if(!pendientes){ // no hay pendientes
				saved = this.actaTransmisionNacionService.guardarTransmision(
						idActa, 
						estadoEnum, 
						usuario, 
						proceso, 
						ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE,
						ConstantesComunes.CERO_INTENTO_TRANSMISION);
				if (saved.isSuccess() && proceso != null) {
					logger.info("Se realiza la sincronizacion: ");
					this.tramsmitirSync(saved.getIdTransmision(), proceso, usuario);
				} else {
					logger.info("No se realiza la sincronizacion: ");
				}
			} else {
				this.actaTransmisionNacionService.guardarTransmision(
						idActa, 
						estadoEnum, 
						usuario, 
						proceso, 
						ConstantesComunes.ESTADO_TRANSMISION_ERROR,
						ConstantesComunes.PRIMER_INTENTO_TRANSMISION);
			}
		}
		
	}
	
	@Override
	@Transactional
	public void sincronizarSync(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario) {
		logger.info("Se inicio el proceso de registrar la transmision: ");
		logger.info("proceso: {}", proceso);
		logger.info("idActa: {}", idActa);
		logger.info("estado: {}", estadoEnum.name());
		
		boolean pendientes = this.actaTransmisionNacionService.hayPendientes(idActa);
		TransmisionCreated saved = null;
		if(!pendientes){ // no hay pendientes
			saved = this.actaTransmisionNacionService.guardarTransmision(
					idActa, 
					estadoEnum, 
					usuario, 
					proceso, 
					ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE,
					ConstantesComunes.CERO_INTENTO_TRANSMISION);
			if (saved.isSuccess() && proceso != null) {
				logger.info("Se realiza la sincronizacion: ");
				this.tramsmitirSync(saved.getIdTransmision(), proceso, usuario);
			} else {
				this.actaTransmisionNacionService.actualizarEstado(
						saved.getIdTransmision(), 
						ConstantesComunes.ESTADO_TRANSMISION_ERROR);
				logger.info("No se realiza la sincronizacion: ");
			}
		} else {
			this.actaTransmisionNacionService.guardarTransmision(
					idActa, 
					estadoEnum, 
					usuario, 
					proceso, 
					ConstantesComunes.ESTADO_TRANSMISION_ERROR,
					ConstantesComunes.CERO_INTENTO_TRANSMISION);
		}
	}

	@Override
	@Transactional
	public void tramsmitirActa(Long idActa, String proceso, String usuario) {
		Optional<Acta> actaOp = this.actaTransmisionNacionService.findByIdActa(idActa);
		logger.info("id acta transmitir: {}", idActa);
		if (actaOp.isPresent()) {

			int cantidad = this.actaTransmisionNacionService.transmisionesBloqueadas(idActa);
			if(cantidad==0){
				List<ActaTransmisionNacion> transmisiones = this.actaTransmisionNacionService.findByIdActaConTransmisionesOrdenadas(idActa);
				for(ActaTransmisionNacion _transmision:transmisiones){
					Optional<ActaTransmisionNacion> transmision = this.actaTransmisionNacionService.findById(_transmision.getId());
					if(transmision.isPresent() && transmision.get().getEstadoTransmitidoNacion().equals(ConstantesComunes.ESTADO_TRANSMISION_ERROR)){
							this.actaTransmisionNacionService.actualizarEstado(
									transmision.get().getId(), 
									ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE);
							this.tramsmitirSync(
									transmision.get().getId(), 
									proceso, 
									"Job");
					}
				}
			}

		}
	}

	
	private void tramsmitirAsync(Long idTransmision, String proceso, String usuarioTransmision) {
		Optional<ActaTransmisionNacion> actaTransmistida = this.actaTransmisionNacionService.findById(idTransmision);
		if (actaTransmistida.isPresent()) {
			logger.info("Se ejecuta la transmision id {}", actaTransmistida.get().getId());
			List<ActaTransmisionNacion> actasTransmistidas = new ArrayList<>();
			actasTransmistidas.add(actaTransmistida.get());
			this.actaTransmisionNacionService.actualizarPreTransmision(actasTransmistidas, usuarioTransmision);

			if (!actasTransmistidas.isEmpty()) {
				TransmisionRequestDto request = new TransmisionRequestDto();
				request.setProceso(proceso);
				request.setActasTransmitidas(adjuntar(mapperRequest(actasTransmistidas)));

				HttpEntity<TransmisionRequestDto> httpEntity = new HttpEntity<>(request, getHeaderTransmision(proceso));
				String url = urlNacion + URL_NACION_RECIBIR_TRANSMISION;
				ResponseEntity<TransmisionResponseDto> response = clientExport.exchange(url, HttpMethod.PATCH,
						httpEntity, TransmisionResponseDto.class);

				processActasTransmitidas(response);

			} else {
				logger.info("No se realizo ninguna transmision ya que no hay ningun cambio para transmitir");
			}
		} else {
			logger.info("No existe la transmision");
		}

	}
	

	private void tramsmitirSync(Long idTransmision, String proceso, String usuarioTransmision) {
		Optional<ActaTransmisionNacion> actaTransmistida = this.actaTransmisionNacionService.findById(idTransmision);
		if (actaTransmistida.isPresent()) {
			try{
				logger.info("Se ejecuta la transmision id {}", actaTransmistida.get().getId());
				List<ActaTransmisionNacion> actasTransmistidas = new ArrayList<>();
				actasTransmistidas.add(actaTransmistida.get());
				this.actaTransmisionNacionService.actualizarPreTransmision(actasTransmistidas, usuarioTransmision);

				if (!actasTransmistidas.isEmpty()) {
					TransmisionRequestDto request = new TransmisionRequestDto();
					request.setProceso(proceso);
					request.setActasTransmitidas(adjuntar(mapperRequest(actasTransmistidas)));

					HttpEntity<TransmisionRequestDto> httpEntity = new HttpEntity<>(request, getHeaderTransmision(proceso));
					String url = urlNacion + URL_NACION_RECIBIR_TRANSMISION;
					ResponseEntity<TransmisionResponseDto> response = clientExport.exchange(url, HttpMethod.PATCH,
							httpEntity, TransmisionResponseDto.class);

					processActasTransmitidas(response);

				} else {
					logger.info("No se realizo ninguna transmision ya que no hay ningun cambio para transmitir");
				}
			}catch (Exception e) {
				this.actaTransmisionNacionService.actualizarEstado(
						idTransmision, 
						ConstantesComunes.ESTADO_TRANSMISION_ERROR, 
						ConstantesComunes.PRIMER_INTENTO_TRANSMISION);
			}
			
		} else {
			logger.info("No existe la transmision");
		}

	}

	private void processActasTransmitidas(ResponseEntity<TransmisionResponseDto> response) {
		TransmisionResponseDto responseData = response.getBody();
		if (response.getStatusCode() == HttpStatus.OK) {
			logger.info("La llamada fue exitosa (código de estado 200)");
			if (responseData != null) {
				this.actaTransmisionNacionService.actualizarPostTransmision(responseData.getActasTransmitidas());
			}
		} else {
			logger.info("La llamada no fue exitosa. Código de estado: {}.", response.getStatusCode().value());
			if (responseData != null) {
				this.actaTransmisionNacionService.actualizarPostTransmision(responseData.getActasTransmitidas());
			}
		}
	}

	private List<TransmisionReqDto> adjuntar(List<TransmisionReqDto> actasTransmistidas) {
		return this.actaTransmisionNacionService.adjuntar(actasTransmistidas);
	}

	
	private List<TransmisionReqDto> mapperRequest(List<ActaTransmisionNacion> actasTransmistidas) {
		return this.actaTransmisionNacionService.mapperRequest(actasTransmistidas);
	}

	@Transactional(readOnly = true)
	public List<ActaTransmisionNacion> listarFaltantesTransmitir(Long idActa) {
		return this.actaTransmisionNacionService.listarFaltantesTransmitir(idActa);
	}

	private HttpHeaders getHeaderTransmision(String proceso) {
		HttpHeaders headers = new HttpHeaders();
		Optional<CentroComputo> opt = this.centroComputoService.getCentroComputoActual();
		if(opt.isPresent() && opt.get()!=null){
			CentroComputo cc = opt.get();
			String token = cc.getApiTokenBackedCc();
			if(token==null || token.isEmpty()){
				throw new IllegalStateException("No se encuentra un token configurado.");
			} // end-if
			headers.setBearerAuth(cc.getApiTokenBackedCc()); 
			headers.set(SceConstantes.USERAGENT_HEADER, SceConstantes.USERAGENT_HEADER_VALUE);
			headers.set(SceConstantes.TENANT_HEADER, proceso);
			headers.set(SceConstantes.HEADER_CODIGO_CC, cc.getCodigo());
			headers.set(SceConstantes.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		} else {
			throw new IllegalStateException("No se encontró el centro de cómputo actual.");
		}
		return headers;
	}


	@Override
	@Transactional
	public void procesarReintentos() {
	    ProcesoElectoral proceso = procesoElectoralService.findByActivo();
	    if (proceso == null) {
	        logger.info("No existe un proceso configurado, el job no se ejecutó.");
	        return;
	    }

	    logger.info("El proceso {} se encuentra activo", proceso.getAcronimo());

	    List<Long> idActas = actaTransmisionNacionService.listarActasNoBloqueadas();
	    if (idActas == null || idActas.isEmpty()) {
	        logger.info("No existen actas para transmitir");
	        return;
	    }

	    idActas.forEach(idActa -> procesarActa(idActa, proceso.getAcronimo()));
	}

	private void procesarActa(Long idActa, String acronimoProceso) {
	    int cantidad = actaTransmisionNacionService.transmisionesBloqueadas(idActa);
	    if (cantidad == 0) return;

	    List<ActaTransmisionNacion> transmisiones = actaTransmisionNacionService
	        .findByIdActaConTransmisionesOrdenadas(idActa);

	    transmisiones.forEach(t -> procesarTransmision(t.getId(), acronimoProceso));
	}

	private void procesarTransmision(Long idTransmision, String acronimoProceso) {
	    Optional<ActaTransmisionNacion> opt = actaTransmisionNacionService.findById(idTransmision);
	    if (opt.isPresent() && ConstantesComunes.ESTADO_TRANSMISION_ERROR.equals(opt.get().getEstadoTransmitidoNacion())) {
	        actaTransmisionNacionService.actualizarEstado(idTransmision, ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE);
	        tramsmitirSync(idTransmision, acronimoProceso, "Job");
	    }
	}


}
