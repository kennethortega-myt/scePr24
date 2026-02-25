package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import pe.gob.onpe.sceorcbackend.exception.InternalServerErrorException;
import pe.gob.onpe.sceorcbackend.model.dto.TransmisionCreated;
import pe.gob.onpe.sceorcbackend.model.dto.transmision.ActaTransmitidaDto;
import pe.gob.onpe.sceorcbackend.model.dto.trazabilidad.ActaTransmisionNacionTrazabilidadDto;
import pe.gob.onpe.sceorcbackend.model.enums.TransmisionNacionEnum;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Archivo;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.CabActaFormato;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.CentroComputo;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Acta;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ActaCeleste;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ActaTransmisionNacion;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActa;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaAccion;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaFormato;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaOficio;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaOpcion;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaPreferencial;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaResolucion;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Formato;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Mesa;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.MiembroMesaCola;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.MiembroMesaSorteado;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.MiembroMesaEscrutinio;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.OmisoMiembroMesa;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.OmisoVotante;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Personero;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.MesaDocumento;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.ActaPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.CabActaFormatoPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.DetActaAccionPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.DetActaFormatoPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.DetActaOpcionPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.DetActaPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.DetActaPreferencialPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.DetActaResolucionPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.DetOficioTransmistirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.MesaArchivoPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.MesaPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.MiembroMesaColaPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.MiembroMesaEscrutinioPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.MiembroMesaSorteadoPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.OficioPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.OmisoMiembroMesaPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.OmisoVotantePorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.PersoneroPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.ResolucionPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.TabFormatoPorTransmitirDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.transmision.json.TransmisionDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.*;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.CentroComputoService;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.ArchivoTransmisionReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.CabActaFormatoPorTransmitirReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.DetActaResolucionPorTransmitirReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.DetOficioTransmistirReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.OficioPorTransmitirReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.ResolucionPorTransmitirReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.TabFormatoPorTransmitirReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.TransmisionReqDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.mapper.transmision.ITransmisionMapper;
import pe.gob.onpe.sceorcbackend.utils.ArchivoUtils;
import pe.gob.onpe.sceorcbackend.utils.ConstanteAccionTransmision;
import pe.gob.onpe.sceorcbackend.utils.ConstantesComunes;
import pe.gob.onpe.sceorcbackend.utils.ConstantesEstadoActa;
import pe.gob.onpe.sceorcbackend.utils.DateUtil;
import pe.gob.onpe.sceorcbackend.utils.PathUtils;
import pe.gob.onpe.sceorcbackend.utils.SceConstantes;


@Service
public class ActaTransmisionNacionServiceImpl implements ActaTransmisionNacionService {

	Logger logger = LoggerFactory.getLogger(ActaTransmisionNacionServiceImpl.class);
	
	@Value("${file.upload-dir}")
    private String imagePath;
	
	private final ActaRepository actaRepository;
	
	private final DetActaPreferencialRepository detActaPreferencialRepository;
	
	private final DetActaOpcionRepository detActaOpcionRepository;
	
	private final ArchivoRepository archivoRepository;

    private final DetActaResolucionRepository detActaResolucionRepository;

    private final DetActaRepository detActaRepository;
    
    private final DetActaAccionRepository detActaAccionRepository;
	
	private final ActaTransmisionNacionRepository actaTransmisionNacionRepository;
	
	private final OmisoVotanteRepository omisoVotanteRepository;

	private final MesaRepository mesaRepository;
	
	private final MiembroMesaSorteadoRepository miembroMesaSorteadoRepository;
	
	private final MiembroMesaEscrutinioRepository miembroMesaEscrutinioRepository;
	
	private final MiembroMesaColaRepository miembroMesaColaRepository;
	
	private final OmisoMiembroMesaRepository omisoMiembroMesaRepository;
	
	private final PersoneroRepository personeroRepository;
	
	private final MesaDocumentoRepository mesaDocumentoRepository;
	
	private final ITransmisionMapper mapper;
	
	private final CentroComputoService centroComputoService;

	private final ObjectMapper objectMapper;
	
	private final ActaCelesteRepository actaCelesteRepository;
	
	private final DetActaOficioRepository detOficioRepository;
	
	private final CabActaFormatoRepository cabActaFormatoRepository;
	
	private final DetActaFormatoRepository detActaFormatoRepository;
	
	private final FormatoRepository tabFormatoRepository;
	
	private static final String CC_FORMATO = "%s-%s";
	
	public ActaTransmisionNacionServiceImpl(
			ActaRepository actaRepository,
			ActaTransmisionNacionRepository actaTransmisionNacionRepository,
			DetActaOpcionRepository detActaOpcionRepository,
			DetActaPreferencialRepository detActaPreferencialRepository,
			ArchivoRepository archivoRepository,
			ITransmisionMapper mapper,
            DetActaResolucionRepository detActaResolucionRepository,
            DetActaRepository detActaRepository,
            MesaRepository mesaRepository,
            OmisoVotanteRepository omisoVotanteRepository,
            MiembroMesaSorteadoRepository miembroMesaSorteadoRepository,
            MiembroMesaColaRepository miembroMesaColaRepository,
            OmisoMiembroMesaRepository omisoMiembroMesaRepository,
            PersoneroRepository personeroRepository,
            MiembroMesaEscrutinioRepository miembroMesaEscrutinioRepository,
            DetActaAccionRepository detActaAccionRepository,
            MesaDocumentoRepository mesaDocumentoRepository,
            CentroComputoService centroComputoService,
            ActaCelesteRepository actaCelesteRepository,
            DetActaOficioRepository detOficioRepository,
            CabActaFormatoRepository cabActaFormatoRepository,
            FormatoRepository tabFormatoRepository,
            DetActaFormatoRepository detActaFormatoRepository
			) {
		this.actaRepository = actaRepository;
		this.detActaPreferencialRepository = detActaPreferencialRepository;
		this.actaTransmisionNacionRepository = actaTransmisionNacionRepository;
		this.detActaOpcionRepository = detActaOpcionRepository;
		this.archivoRepository = archivoRepository;
		this.mapper = mapper;
        this.detActaResolucionRepository =detActaResolucionRepository;
        this.detActaRepository = detActaRepository;
        this.mesaRepository = mesaRepository;
        this.omisoVotanteRepository = omisoVotanteRepository;
        this.miembroMesaSorteadoRepository = miembroMesaSorteadoRepository;
        this.miembroMesaColaRepository = miembroMesaColaRepository;
        this.omisoMiembroMesaRepository = omisoMiembroMesaRepository;
        this.personeroRepository = personeroRepository;
        this.miembroMesaEscrutinioRepository = miembroMesaEscrutinioRepository;
        this.detActaAccionRepository = detActaAccionRepository;
        this.mesaDocumentoRepository = mesaDocumentoRepository;
        this.centroComputoService = centroComputoService;
        this.actaCelesteRepository =  actaCelesteRepository;
        this.detOficioRepository = detOficioRepository;
        this.cabActaFormatoRepository = cabActaFormatoRepository;
        this.tabFormatoRepository = tabFormatoRepository;
        this.detActaFormatoRepository = detActaFormatoRepository; 
        this.objectMapper = new ObjectMapper();
	}
	

	@Override
	@Transactional(readOnly = true)
	public Optional<ActaTransmisionNacion> findById(Long idTransmision){
		return this.actaTransmisionNacionRepository.findById(idTransmision);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TransmisionCreated guardarTransmision(
												Long idActa, 
												TransmisionNacionEnum estadoEnum, 
												String usuario, 
												String proceso, 
												Integer estadoTransmision,
												Integer intento) {
		
		Long id = null;
		Optional<Acta> actaOp = this.actaRepository.findById(idActa);
		TransmisionCreated x = new TransmisionCreated();
		x.setSuccess(Boolean.TRUE);
		Integer nTransmite = ConstantesComunes.ACTIVO;
		if (actaOp.isPresent()) {
			logger.info("se procede a guardar el acta transmitida");
            if (estadoEnum.equals(TransmisionNacionEnum.INITIAL_TRANSMISION)|| estadoEnum.equals(TransmisionNacionEnum.ACTA_PENDIENTE_TRANSMISION)
                ||estadoEnum.equals(TransmisionNacionEnum.DIGTAL_RECIBIDA_TRANSMISION)) {
                x.setSuccess(Boolean.FALSE);
                nTransmite = ConstantesComunes.INACTIVO;
            }
            
            ActaTransmisionNacion actaTransmitida = new ActaTransmisionNacion();
            actaTransmitida.setIdActa(idActa);
            actaTransmitida.setTransmite(nTransmite);
            actaTransmitida.setTipoTransmision(estadoEnum.name());
            actaTransmitida.setEstadoTransmitidoNacion(estadoTransmision);
            if(nTransmite.equals(ConstantesComunes.INACTIVO)){
            	actaTransmitida.setEstadoTransmitidoNacion(ConstantesComunes.ESTADO_TRANSMISION_ERROR);
            }
            actaTransmitida.setAccion(this.determinarAccion(estadoEnum));
            if(nTransmite.equals(ConstantesComunes.INACTIVO)){
            	actaTransmitida.setIntento(ConstantesComunes.CERO_INTENTO_TRANSMISION);
            } else {
            	actaTransmitida.setIntento(intento);
            }
            actaTransmitida.setFechaTransmision(DateUtil.getFechaActualPeruana());
            actaTransmitida.setUsuarioTransmision(usuario);
            actaTransmitida.setFechaCreacion(DateUtil.getFechaActualPeruana());
            actaTransmitida.setUsuarioCreacion(usuario);
            actaTransmitida.setActivo(ConstantesComunes.ACTIVO);
            this.actaTransmisionNacionRepository.save(actaTransmitida);
            TransmisionDto request = buildRequestTransmision(actaTransmitida, actaOp, estadoEnum, proceso);
            actaTransmitida.setRequestActaTransmision(request);
        	this.actaTransmisionNacionRepository.save(actaTransmitida);
        	id = actaTransmitida.getId();
        	x.setIdTransmision(id);
		} else {
			logger.info("no se encuentra el acta");
		}
		
		return x;
	}
	

    protected TransmisionDto buildRequestTransmision(ActaTransmisionNacion actaTransmisionNacion,
			Optional<Acta> actaOp,
			TransmisionNacionEnum estadoEnum,
			String proceso) {
		TransmisionDto transmision = null;
		switch (actaTransmisionNacion.getAccion()) {
	        case ConstanteAccionTransmision.ACCION_ACTA,
	             ConstanteAccionTransmision.ACCION_MESA_DETALLE -> {
	            transmision = new TransmisionDto();
	            transmision.setCentroComputo(this.getCodigoCentroComputoActual());
	            transmision.setAccion(actaTransmisionNacion.getAccion());
	            transmision.setActaTransmitida(this.mapActa(actaOp, estadoEnum));
	            mapCommonFields(transmision, actaTransmisionNacion, proceso);  
	        }
	        case ConstanteAccionTransmision.ACCION_SOBRE_CELESTE -> {
	        	transmision = new TransmisionDto();
	            transmision.setCentroComputo(this.getCodigoCentroComputoActual());
	            transmision.setAccion(actaTransmisionNacion.getAccion());
	            transmision.setActaTransmitida(this.mapActaCeleste(actaOp));
	            mapCommonFields(transmision, actaTransmisionNacion, proceso);
	        }
	        default -> {
	            // No hacer nada
	        }
	    }
		logger.info("termino el mensaje");
		return transmision;
	}
	
	
	@Override
	@Transactional(readOnly = true)
    public List<TransmisionReqDto> mapperRequest(List<ActaTransmisionNacion> actasTransmistidas){
		return actasTransmistidas
		.stream()
		.map(t -> this.mapper.toDto(t.getRequestActaTransmision()))
		.toList();
	}
	
	@Override
	@Transactional(readOnly = true)
    public List<TransmisionReqDto> adjuntar(List<TransmisionReqDto> actasTransmistidas){
		return actasTransmistidas.stream()
		        .map(acta -> {
		        	acta.getActaTransmitida().setArchivoEscrutinio(getArchivoTransmisionReqDto(acta.getActaTransmitida().getIdArchivoEscrutinio()));
		        	acta.getActaTransmitida().setArchivoInstalacionSufragio(getArchivoTransmisionReqDto(acta.getActaTransmitida().getIdArchivoInstalacionSufragio()));
	                List<DetActaResolucionPorTransmitirReqDto> resoluciones = acta.getActaTransmitida().getResoluciones();
	                if (resoluciones != null && !resoluciones.isEmpty()) {
	                    List<DetActaResolucionPorTransmitirReqDto> resolucionesDto = resoluciones.stream()
	                        .map(resolucion -> {
	                            ResolucionPorTransmitirReqDto rt = resolucion.getResolucionDto();
	                            if (rt != null) {
	                                rt.setArchivoResolucion(
	                                    getArchivoTransmisionReqDto(rt.getIdArchivoResolucion())
	                                );
	                                resolucion.setResolucionDto(rt);
	                            }
	                            return resolucion;
	                        })
	                        .toList();
	                    acta.getActaTransmitida().setResoluciones(resolucionesDto);
	                }
	                List<DetOficioTransmistirReqDto> detOficios = acta.getActaTransmitida().getDetallesOficio();
	                if(detOficios!= null && !detOficios.isEmpty()){
	                	List<DetOficioTransmistirReqDto> detOficiosDtos = detOficios.stream()
	                	.map( detOficio -> {
	                		OficioPorTransmitirReqDto ot = detOficio.getCabOficio();
	                		if(ot != null){
	                			ot.setArchivo(getArchivoTransmisionReqDto(ot.getIdArchivo()));
	                			detOficio.setCabOficio(ot);
	                		} // end-if
	                		
	                		CabActaFormatoPorTransmitirReqDto caf = detOficio.getCabActaFormato();
	                        if (caf != null) {
	                            caf.setArchivo(getArchivoTransmisionReqDto(caf.getIdArchivo()));
	                            TabFormatoPorTransmitirReqDto tf = caf.getTabFormato();
	                            if (tf != null) {
	                                tf.setArchivo(getArchivoTransmisionReqDto(tf.getIdArchivo()));
	                                caf.setTabFormato(tf);
	                            } // end-if
	                            detOficio.setCabActaFormato(caf);
	                        } // end-if
	                		
	                		return detOficio;
	                	})
	                	.toList();
	                	acta.getActaTransmitida().setDetallesOficio(detOficiosDtos);
	                }
		            return acta;
		        })
		        .toList();
	}
	
	private String getEstadoActa(String estadoActa, TransmisionNacionEnum estadoEnum) {
		if (estadoEnum.equals(TransmisionNacionEnum.REPROCESO)) {
            return ConstantesEstadoActa.ESTADO_ACTA_REPROCESADA_NORMAL;
        }
		return estadoActa;
	}
	
	
    protected ArchivoTransmisionReqDto getArchivoTransmisionReqDto(Long id) {
		ArchivoTransmisionReqDto dto = null;
		if(id!=null) {
			Optional<Archivo> archivo = this.archivoRepository.findById(id);
			if (archivo.isPresent()) {
				try {
					String base64 = convertToBase64(PathUtils.normalizePath(this.imagePath, archivo.get().getGuid()));
					dto = ArchivoTransmisionReqDto
							.builder()
							.base64(base64)
							.mimeType(archivo.get().getFormato())
							.guid(archivo.get().getGuid())
							.extension(ArchivoUtils.getExtension(archivo.get().getFormato()))
							.build();
				} catch (Exception e) {
					logger.error("error", e);
					return null;
				}
			}
		}
		return dto;
	}
    
    protected ActaPorTransmitirDto mapActaCeleste(Optional<Acta> actaOp) {
    	ActaPorTransmitirDto actaTransmitida = new ActaPorTransmitirDto();
        if (actaOp.isPresent()) {
        	Optional<ActaCeleste> actaCelesteOp = this.actaCelesteRepository.findByActa_Id(actaOp.get().getId());
        	if(actaCelesteOp.isPresent()){
        		ActaPorTransmitirDto actaCelesteTransmitidaDto = new ActaPorTransmitirDto();
        		ActaCeleste actaCeleste = actaCelesteOp.get();
        		actaCelesteTransmitidaDto.setIdActa(actaCeleste.getActa().getId());
                actaCelesteTransmitidaDto.setIdActaCeleste(actaCeleste.getId());
                actaCelesteTransmitidaDto.setIdCcCeleste(String.format(CC_FORMATO, this.getCodigoCentroComputoActual(), actaCeleste.getId().toString()));
                actaCelesteTransmitidaDto.setIdArchivoEscrutinio(actaCeleste.getArchivoEscrutinio()!=null ? actaCeleste.getArchivoEscrutinio().getId():null);
                actaCelesteTransmitidaDto.setIdArchivoInstalacionSufragio(actaCeleste.getArchivoInstalacionSufragio()!=null ? actaCeleste.getArchivoInstalacionSufragio().getId():null);
                actaCelesteTransmitidaDto.setNumeroCopia(actaCeleste.getNumeroCopia());
                actaCelesteTransmitidaDto.setDigitoChequeoEscrutinio(actaCeleste.getDigitoChequeoEscrutinio());
                actaCelesteTransmitidaDto.setDigitoChequeoInstalacion(actaCeleste.getDigitoChequeoInstalacion());
                actaCelesteTransmitidaDto.setDigitoChequeoSufragio(actaCeleste.getDigitoChequeoSufragio());
                actaCelesteTransmitidaDto.setEstadoDigitalizacion(actaCeleste.getEstadoDigitalizacion());
                actaCelesteTransmitidaDto.setDigitalizacionEscrutinio(actaCeleste.getDigitalizacionEscrutinio());
                actaCelesteTransmitidaDto.setDigitalizacionInstalacion(actaCeleste.getDigitalizacionInstalacion());
                actaCelesteTransmitidaDto.setDigitalizacionSufragio(actaCeleste.getDigitalizacionSufragio());
                actaCelesteTransmitidaDto.setDigitalizacionInstalacionSufragio(actaCeleste.getDigitalizacionInstalacionSufragio());
                actaCelesteTransmitidaDto.setObservDigEscrutinio(actaCeleste.getObservDigEscrutinio());
                actaCelesteTransmitidaDto.setObservDigInstalacionSufragio(actaCeleste.getObservDigInstalacionSufragio());
                actaCelesteTransmitidaDto.setActivo(actaCeleste.getActivo());
                actaCelesteTransmitidaDto.setAsignado(actaCeleste.getAsignado());
                actaCelesteTransmitidaDto.setAudFechaCreacion(DateUtil.getDateString(actaCeleste.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
                actaCelesteTransmitidaDto.setAudFechaModificacion(DateUtil.getDateString(actaCeleste.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
                actaCelesteTransmitidaDto.setAudUsuarioCreacion(actaCeleste.getUsuarioCreacion());
                actaCelesteTransmitidaDto.setAudUsuarioModificacion(actaCeleste.getUsuarioModificacion());
                actaCelesteTransmitidaDto.setDetallesOficio(mapOficioByIdActaCeleste(actaCeleste.getId()));
                actaTransmitida.setIdActa(actaCeleste.getActa().getId());
                actaTransmitida.setEstadoActa(actaCeleste.getActa().getEstadoActa());
                actaTransmitida.setAudFechaCreacion(DateUtil.getDateString(actaCeleste.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
                actaTransmitida.setAudFechaModificacion(DateUtil.getDateString(actaCeleste.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
                actaTransmitida.setAudUsuarioCreacion(actaCeleste.getUsuarioCreacion());
                actaTransmitida.setAudUsuarioModificacion(actaCeleste.getUsuarioModificacion());
                actaTransmitida.setActaCeleste(actaCelesteTransmitidaDto); 
        	}
        }
       
        
        return actaTransmitida;
    }
	
    protected ActaPorTransmitirDto mapActa(Optional<Acta> actaOp, TransmisionNacionEnum estadoEnum) {
    	
        ActaPorTransmitirDto actaTransmitidaDto = new ActaPorTransmitirDto();
        Acta acta = null;
        if (actaOp.isPresent()) {
        	acta = actaOp.get();
        	logger.info("Estado del acta: {}", acta.getEstadoActa());
            actaTransmitidaDto.setIdActa(acta.getId());
            actaTransmitidaDto.setIdMesa(acta.getMesa()!=null ? acta.getMesa().getId() : null);
            if(acta.getMesa()!=null) {
            	actaTransmitidaDto.setMesa(mapMesa(acta.getMesa().getId()));
            }
            actaTransmitidaDto.setIdArchivoEscrutinio(acta.getArchivoEscrutinio()!=null ? acta.getArchivoEscrutinio().getId():null);
            actaTransmitidaDto.setIdArchivoInstalacionSufragio(acta.getArchivoInstalacionSufragio()!=null ? acta.getArchivoInstalacionSufragio().getId():null);
            actaTransmitidaDto.setIdDetUbigeoEleccion(acta.getUbigeoEleccion()!=null ? acta.getUbigeoEleccion().getId():null);
            actaTransmitidaDto.setNumeroCopia(acta.getNumeroCopia());
            actaTransmitidaDto.setNumeroLote(acta.getNumeroLote());
            actaTransmitidaDto.setDigitoChequeoEscrutinio(acta.getDigitoChequeoEscrutinio());
            actaTransmitidaDto.setDigitoChequeoInstalacion(acta.getDigitoChequeoInstalacion());
            actaTransmitidaDto.setDigitoChequeoSufragio(acta.getDigitoChequeoSufragio());
            actaTransmitidaDto.setTipoLote(acta.getTipoLote());
            actaTransmitidaDto.setElectoresHabiles(acta.getElectoresHabiles());
            actaTransmitidaDto.setCvas(acta.getCvas());
            actaTransmitidaDto.setCvasAutomatico(acta.getCvasAutomatico());
            actaTransmitidaDto.setCvasv1(acta.getCvasV1());
            actaTransmitidaDto.setCvasv2(acta.getCvasV2());
            actaTransmitidaDto.setIlegibleCvas(acta.getIlegibleCvas());
            actaTransmitidaDto.setIlegibleCvasv1(acta.getIlegibleCvasV1());
            actaTransmitidaDto.setIlegibleCvasv2(acta.getIlegibleCvasV2());
            actaTransmitidaDto.setVotosCalculados(acta.getVotosCalculados());
            actaTransmitidaDto.setTotalVotos(acta.getTotalVotos());
            actaTransmitidaDto.setEstadoActaOriginal(acta.getEstadoActa());
            actaTransmitidaDto.setEstadoActa(getEstadoActa(acta.getEstadoActa(),estadoEnum));
            actaTransmitidaDto.setEstadoCc(acta.getEstadoCc());
            actaTransmitidaDto.setEstadoActaResolucion(acta.getEstadoActaResolucion());
            actaTransmitidaDto.setEstadoDigitalizacion(acta.getEstadoDigitalizacion());
            actaTransmitidaDto.setEstadoErrorMaterial(acta.getEstadoErrorMaterial());
            actaTransmitidaDto.setDigitalizacionEscrutinio(acta.getDigitalizacionEscrutinio());
            actaTransmitidaDto.setDigitalizacionInstalacionSufragio(acta.getDigitalizacionInstalacionSufragio());
            actaTransmitidaDto.setControlDigEscrutinio(acta.getControlDigEscrutinio());
            actaTransmitidaDto.setControlDigInstalacionSufragio(acta.getControlDigInstalacionSufragio());
            actaTransmitidaDto.setObservDigEscrutinio(acta.getObservDigEscrutinio());
            actaTransmitidaDto.setObservDigInstalacionSufragio(acta.getObservDigInstalacionSufragio());
            actaTransmitidaDto.setDigitacionHoras(acta.getDigitacionHoras());
            actaTransmitidaDto.setDigitacionVotos(acta.getDigitacionVotos());
            actaTransmitidaDto.setDigitacionObserv(acta.getDigitacionObserv());
            actaTransmitidaDto.setDigitacionFirmasAutomatico(acta.getDigitacionFirmasAutomatico());
            actaTransmitidaDto.setDigitacionFirmasManual(acta.getDigitacionFirmasManual());
            actaTransmitidaDto.setControlDigitacion(acta.getControlDigitacion());
            actaTransmitidaDto.setHoraEscrutinioAutomatico(acta.getHoraEscrutinioAutomatico());
            actaTransmitidaDto.setHoraEscrutinioManual(acta.getHoraEscrutinioManual());
            actaTransmitidaDto.setHoraInstalacionAutomatico(acta.getHoraInstalacionAutomatico());
            actaTransmitidaDto.setHoraInstalacionManual(acta.getHoraInstalacionManual());
            actaTransmitidaDto.setDescripcionObservAutomatico(acta.getDescripcionObservAutomatico());
            actaTransmitidaDto.setEscrutinioFirmaMm1Automatico(acta.getEscrutinioFirmaMm1Automatico());
            actaTransmitidaDto.setEscrutinioFirmaMm2Automatico(acta.getEscrutinioFirmaMm2Automatico());
            actaTransmitidaDto.setEscrutinioFirmaMm3Automatico(acta.getEscrutinioFirmaMm3Automatico());
            actaTransmitidaDto.setInstalacionFirmaMm1Automatico(acta.getInstalacionFirmaMm1Automatico());
            actaTransmitidaDto.setInstalacionFirmaMm2Automatico(acta.getInstalacionFirmaMm2Automatico());
            actaTransmitidaDto.setInstalacionFirmaMm3Automatico(acta.getInstalacionFirmaMm3Automatico());
            actaTransmitidaDto.setSufragioFirmaMm1Automatico(acta.getSufragioFirmaMm1Automatico());
            actaTransmitidaDto.setSufragioFirmaMm2Automatico(acta.getSufragioFirmaMm2Automatico());
            actaTransmitidaDto.setSufragioFirmaMm3Automatico(acta.getSufragioFirmaMm3Automatico());
            actaTransmitidaDto.setTipoTransmision(acta.getTipoTransmision());
            actaTransmitidaDto.setActivo(acta.getActivo());
            actaTransmitidaDto.setVerificador(acta.getVerificador());
            actaTransmitidaDto.setVerificador2(acta.getVerificador2());
            actaTransmitidaDto.setAudFechaCreacion(DateUtil.getDateString(acta.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
            actaTransmitidaDto.setAudFechaModificacion(DateUtil.getDateString(acta.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
            actaTransmitidaDto.setAudUsuarioCreacion(acta.getUsuarioCreacion());
            actaTransmitidaDto.setAudUsuarioModificacion(acta.getUsuarioModificacion());
            actaTransmitidaDto.setDetalle(mapDetallesActa(acta.getId()));
            actaTransmitidaDto.setResoluciones(mapResoluciones(acta.getId()));
            actaTransmitidaDto.setAcciones(mapDetallesAccion(acta.getId()));
            actaTransmitidaDto.setSolucionTecnologica(acta.getSolucionTecnologica());
        }
        
        return actaTransmitidaDto;
    }
	

	protected MesaPorTransmitirDto mapMesa(Long idMesa) {
		MesaPorTransmitirDto mesaDto = null;
		Optional<Mesa> mesa = mesaRepository.findById(idMesa);
		if(mesa.isPresent()) {
			mesaDto = new MesaPorTransmitirDto();
			mesaDto.setIdMesa(idMesa);
			mesaDto.setEstado(mesa.get().getEstadoMesa());
			
			// states
			mesaDto.setEstadoDigitalizacionLe(mesa.get().getEstadoDigitalizacionLe());
			mesaDto.setEstadoDigitalizacionMm(mesa.get().getEstadoDigitalizacionMm());
			mesaDto.setEstadoDigitalizacionPr(mesa.get().getEstadoDigitalizacionPr());
			mesaDto.setEstadoDigitalizacionMe(mesa.get().getEstadoDigitalizacionMe());
			
			//users
			mesaDto.setUsuarioAsignadoLe(mesa.get().getUsuarioAsignadoLe());
			mesaDto.setUsuarioAsignadoMm(mesa.get().getUsuarioAsignadoMm());
			mesaDto.setUsuarioAsignadoPr(mesa.get().getUsuarioAsignadoPr());
			mesaDto.setUsuarioAsignadoMe(mesa.get().getUsuarioAsignadoMe());
			
			// date
			mesaDto.setFechaAsignadoLe(DateUtil.getDateString(mesa.get().getFechaAsignadoLe(), SceConstantes.FORMATO_FECHA_TRANSMISION));
			mesaDto.setFechaAsignadoMm(DateUtil.getDateString(mesa.get().getFechaAsignadoMm(), SceConstantes.FORMATO_FECHA_TRANSMISION));
			mesaDto.setFechaAsignadoPr(DateUtil.getDateString(mesa.get().getFechaAsignadoPr(), SceConstantes.FORMATO_FECHA_TRANSMISION));
			mesaDto.setFechaAsignadoMe(DateUtil.getDateString(mesa.get().getFechaAsignadoMe(), SceConstantes.FORMATO_FECHA_TRANSMISION));
			
			mesaDto.setActivo(mesa.get().getActivo());
			mesaDto.setUsuarioCreacion(mesa.get().getUsuarioCreacion());
			mesaDto.setUsuarioModificacion(mesa.get().getUsuarioModificacion());
			mesaDto.setFechaCreacion(DateUtil.getDateString(mesa.get().getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
			mesaDto.setFechaModificacion(DateUtil.getDateString(mesa.get().getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
			mesaDto.setOmisosVotantes(mapOmivosVotantes(idMesa));
			mesaDto.setMiembrosMesaSorteado(miembrosMesaSorteado(idMesa));
			mesaDto.setMiembrosMesaCola(miembrosMesaCola(idMesa));
			mesaDto.setDetalleArchivo(detallesArchivo(idMesa));
			mesaDto.setMiembrosMesaEscrutinio(miembrosMesaEscrutinio(idMesa));
			mesaDto.setPersoneros(mapPersoneros(idMesa));
		}
		return mesaDto;
	}
	
	
	
	private List<PersoneroPorTransmitirDto> mapPersoneros(Long idMesa){
		List<PersoneroPorTransmitirDto> personerosDto = null;
		List<Personero> personeros = personeroRepository.findByIdMesa(idMesa);
		if(personeros!=null) {
			personerosDto = new ArrayList<>();
			PersoneroPorTransmitirDto personeroDto = null;
			for(Personero personero:personeros) {
				personeroDto = new PersoneroPorTransmitirDto();
				personeroDto.setIdPersonero(personero.getId());
				personeroDto.setIdMesa(idMesa);
				if(personero.getAgrupacionPolitica()!=null) {
					personeroDto.setIdAgrupacionPolitica(personero.getAgrupacionPolitica().getId());
				}
				personeroDto.setDocumentoIdentidad(personero.getDocumentoIdentidad());
				personeroDto.setNombres(personero.getNombres());
				personeroDto.setApellidoPaterno(personero.getApellidoPaterno());
				personeroDto.setApellidoMaterno(personero.getApellidoMaterno());
				personeroDto.setActivo(personero.getActivo());
				personeroDto.setAudUsuarioCreacion(personero.getUsuarioCreacion());
				personeroDto.setAudUsuarioModificacion(personero.getUsuarioModificacion());
				personeroDto.setAudFechaCreacion(DateUtil.getDateString(personero.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				personeroDto.setAudFechaModificacion(DateUtil.getDateString(personero.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				personerosDto.add(personeroDto);
			}
		}
		return personerosDto;
	}
	

	protected List<OmisoVotantePorTransmitirDto> mapOmivosVotantes(Long idMesa) {
		List<OmisoVotantePorTransmitirDto> omisosDto = null;
		List<OmisoVotante> omisos = omisoVotanteRepository.findByMesaId(idMesa);
		if(omisos!=null) {
			omisosDto = new ArrayList<>();
			OmisoVotantePorTransmitirDto omisoDto = null;
			for(OmisoVotante omiso:omisos) {
				omisoDto = new OmisoVotantePorTransmitirDto();
				omisoDto.setIdMesa(idMesa);
				omisoDto.setIdPadronElectoral(omiso.getPadronElectoral()!=null ? omiso.getPadronElectoral().getId() : null);
				omisoDto.setActivo(omiso.getActivo());
				omisoDto.setUsuarioCreacion(omiso.getUsuarioCreacion());
				omisoDto.setUsuarioModificacion(omiso.getUsuarioModificacion());
				omisoDto.setFechaCreacion(DateUtil.getDateString(omiso.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				omisoDto.setFechaModificacion(DateUtil.getDateString(omiso.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				omisosDto.add(omisoDto);
			}
		}
		return omisosDto;
	}
	
	protected List<MiembroMesaSorteadoPorTransmitirDto> miembrosMesaSorteado(Long idMesa){
		List<MiembroMesaSorteadoPorTransmitirDto> omisosMesaSorteadoDto = null;
		List<MiembroMesaSorteado> miembrosMesaSorteado = miembroMesaSorteadoRepository.findByMesaId(idMesa);
		if(miembrosMesaSorteado!=null) {
			omisosMesaSorteadoDto = new ArrayList<>();
			MiembroMesaSorteadoPorTransmitirDto miembroMesaSorteadoDto = null;
			for(MiembroMesaSorteado miembroMesaSorteado:miembrosMesaSorteado) {
				miembroMesaSorteadoDto = new MiembroMesaSorteadoPorTransmitirDto();
				miembroMesaSorteadoDto.setIdMiembroMesaSorteado(miembroMesaSorteado.getId());
				miembroMesaSorteadoDto.setIdMesa(idMesa);
				miembroMesaSorteadoDto.setOmisosMiembroMesa(omisosMiembrosMesas(miembroMesaSorteado.getId(),idMesa));
				miembroMesaSorteadoDto.setAsistenciaAutomatico(miembroMesaSorteado.getAsistenciaAutomatico());
				miembroMesaSorteadoDto.setAsistenciaManual(miembroMesaSorteado.getAsistenciaManual());
				miembroMesaSorteadoDto.setActivo(miembroMesaSorteado.getActivo());
				miembroMesaSorteadoDto.setUsuarioCreacion(miembroMesaSorteado.getUsuarioCreacion());
				miembroMesaSorteadoDto.setUsuarioModificacion(miembroMesaSorteado.getUsuarioModificacion());
				miembroMesaSorteadoDto.setFechaCreacion(DateUtil.getDateString(miembroMesaSorteado.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				miembroMesaSorteadoDto.setFechaModificacion(DateUtil.getDateString(miembroMesaSorteado.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				omisosMesaSorteadoDto.add(miembroMesaSorteadoDto);
			}
		}
		return omisosMesaSorteadoDto;
	}
	
	protected List<MiembroMesaEscrutinioPorTransmitirDto> miembrosMesaEscrutinio(Long idMesa){
		List<MiembroMesaEscrutinioPorTransmitirDto> miembrosMesaEscrutinioDto = null;
		List<MiembroMesaEscrutinio> miembrosMesaEscrutinio = miembroMesaEscrutinioRepository.findByMesaId(idMesa);
		if(miembrosMesaEscrutinio!=null) {
			miembrosMesaEscrutinioDto = new ArrayList<>();
			MiembroMesaEscrutinioPorTransmitirDto miembroMesaEscrutinioDto = null;
			for(MiembroMesaEscrutinio miembroMesaEscrutinio:miembrosMesaEscrutinio) {
				miembroMesaEscrutinioDto = new MiembroMesaEscrutinioPorTransmitirDto();
				miembroMesaEscrutinioDto.setIdMiembroMesaEscrutinio(miembroMesaEscrutinio.getId());
				miembroMesaEscrutinioDto.setIdMiembroMesaEscrutinioCc(String.format(CC_FORMATO, this.getCodigoCentroComputoActual(), miembroMesaEscrutinio.getId().toString()));				
				miembroMesaEscrutinioDto.setIdMesa(idMesa);
				miembroMesaEscrutinioDto.setDocumentoIdentidadPresidente(miembroMesaEscrutinio.getDocumentoIdentidadPresidente());
				miembroMesaEscrutinioDto.setDocumentoIdentidadSecretario(miembroMesaEscrutinio.getDocumentoIdentidadSecretario());
				miembroMesaEscrutinioDto.setDocumentoIdentidadTercerMiembro(miembroMesaEscrutinio.getDocumentoIdentidadTercerMiembro());
				miembroMesaEscrutinioDto.setActivo(miembroMesaEscrutinio.getActivo());
				miembroMesaEscrutinioDto.setUsuarioCreacion(miembroMesaEscrutinio.getUsuarioCreacion());
				miembroMesaEscrutinioDto.setUsuarioModificacion(miembroMesaEscrutinio.getUsuarioModificacion());
				miembroMesaEscrutinioDto.setFechaCreacion(DateUtil.getDateString(miembroMesaEscrutinio.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				miembroMesaEscrutinioDto.setFechaModificacion(DateUtil.getDateString(miembroMesaEscrutinio.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				miembrosMesaEscrutinioDto.add(miembroMesaEscrutinioDto);
			}
		}
		return miembrosMesaEscrutinioDto;
	}

	protected List<OmisoMiembroMesaPorTransmitirDto> omisosMiembrosMesas(Long idMiembroMesaSorteado, Long idMesa){
		List<OmisoMiembroMesaPorTransmitirDto> omisosMiembroMesaDto = null;
		List<OmisoMiembroMesa> omisosMiembroMesa = omisoMiembroMesaRepository.findByMiembroMesaSorteadoId(idMiembroMesaSorteado);
		if(omisosMiembroMesa!=null) {
			omisosMiembroMesaDto = new ArrayList<>();
			OmisoMiembroMesaPorTransmitirDto omisoMiembroMesaDto = null;
			for(OmisoMiembroMesa omisoMiembroMesa:omisosMiembroMesa) {
				omisoMiembroMesaDto = new OmisoMiembroMesaPorTransmitirDto();
				omisoMiembroMesaDto.setIdOmisoMiembroMesa(omisoMiembroMesa.getId());
				omisoMiembroMesaDto.setIdOmisoMiembroMesaCc(String.format(CC_FORMATO, this.getCodigoCentroComputoActual(), omisoMiembroMesa.getId().toString()));
				omisoMiembroMesaDto.setIdMiembroMesaSorteado(idMiembroMesaSorteado);
				omisoMiembroMesaDto.setIdMesa(idMesa);
				omisoMiembroMesaDto.setActivo(omisoMiembroMesa.getActivo());
				omisoMiembroMesaDto.setUsuarioCreacion(omisoMiembroMesa.getUsuarioCreacion());
				omisoMiembroMesaDto.setUsuarioModificacion(omisoMiembroMesa.getUsuarioModificacion());
				omisoMiembroMesaDto.setFechaCreacion(DateUtil.getDateString(omisoMiembroMesa.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				omisoMiembroMesaDto.setFechaModificacion(DateUtil.getDateString(omisoMiembroMesa.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				omisosMiembroMesaDto.add(omisoMiembroMesaDto);
			}
		}
		return omisosMiembroMesaDto;
	}
	
	protected List<MiembroMesaColaPorTransmitirDto> miembrosMesaCola(Long idMesa){
		List<MiembroMesaColaPorTransmitirDto> miembrosMesaColaDto = null;
		List<MiembroMesaCola> miembrosMesaCola = miembroMesaColaRepository.findByMesaId(idMesa);
		if(miembrosMesaCola!=null) {
			miembrosMesaColaDto = new ArrayList<>();
			MiembroMesaColaPorTransmitirDto miembroMesaColaDto = null;
			for(MiembroMesaCola miembroMesaCola:miembrosMesaCola) {
				miembroMesaColaDto = new MiembroMesaColaPorTransmitirDto();
				miembroMesaColaDto.setIdPadronElectoral(miembroMesaCola.getPadronElectoral()!=null 
						? miembroMesaCola.getPadronElectoral().getId()
						: null);
				miembroMesaColaDto.setIdMesa(idMesa);
				miembroMesaColaDto.setCargo(miembroMesaCola.getCargo());
				miembroMesaColaDto.setActivo(miembroMesaCola.getActivo());
				miembroMesaColaDto.setUsuarioCreacion(miembroMesaCola.getUsuarioCreacion());
				miembroMesaColaDto.setUsuarioModificacion(miembroMesaCola.getUsuarioModificacion());
				miembroMesaColaDto.setFechaCreacion(DateUtil.getDateString(miembroMesaCola.getFechaCreacion(), SceConstantes.PATTERN_DD_MM_YYYY_HH_MM_SS_DASH));
				miembroMesaColaDto.setFechaModificacion(DateUtil.getDateString(miembroMesaCola.getFechaModificacion(), SceConstantes.PATTERN_DD_MM_YYYY_HH_MM_SS_DASH));
				miembrosMesaColaDto.add(miembroMesaColaDto);
			}
		}
		return miembrosMesaColaDto;
	}
	

	protected List<MesaArchivoPorTransmitirDto> detallesArchivo(Long idMesa){
		List<MesaArchivoPorTransmitirDto> detallesArchivoDto = null;
		List<MesaDocumento> mesasDocumento = mesaDocumentoRepository.findByMesaId(idMesa);
		if(mesasDocumento!=null) {
			detallesArchivoDto = new ArrayList<>();
			MesaArchivoPorTransmitirDto detalleArchivoDto = null;
			for(MesaDocumento mesaDocumento:mesasDocumento) {
				detalleArchivoDto = new MesaArchivoPorTransmitirDto();
				detalleArchivoDto.setIdMesa(idMesa);
				detalleArchivoDto.setIdDocumentoElectoral(mesaDocumento.getAdmDocumentoElectoral().getId());
				detalleArchivoDto.setIdMesaArchivoCc(String.format(CC_FORMATO, this.getCodigoCentroComputoActual(), mesaDocumento.getId().toString()));
				detalleArchivoDto.setTipoArchivo(mesaDocumento.getTipoArchivo());
				detalleArchivoDto.setPagina(mesaDocumento.getPagina());
				detalleArchivoDto.setDescripcionObservacion(mesaDocumento.getDescripcionObservacion());
				detalleArchivoDto.setDigitalizacion(mesaDocumento.getDigitalizacion());
				detalleArchivoDto.setEstadoDigitalizacion(mesaDocumento.getEstadoDigitalizacion());				
				detalleArchivoDto.setObservacionDigitalizacion(mesaDocumento.getObservacionDigitalizacion());	
				detalleArchivoDto.setActivo(mesaDocumento.getActivo());
				detalleArchivoDto.setUsuarioCreacion(mesaDocumento.getUsuarioCreacion());
				detalleArchivoDto.setUsuarioModificacion(mesaDocumento.getUsuarioModificacion());
				detalleArchivoDto.setFechaCreacion(DateUtil.getDateString(mesaDocumento.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				detalleArchivoDto.setFechaModificacion(DateUtil.getDateString(mesaDocumento.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
				detallesArchivoDto.add(detalleArchivoDto);
			}
		}
		return detallesArchivoDto;
	}


    protected List<DetActaPorTransmitirDto> mapDetallesActa(Long idActa)
    {
        List<DetActaPorTransmitirDto> detActaTransmitidaDto = null;
        List<DetActa> detallesActa = this.detActaRepository.findByActa_Id(idActa);
        if (detallesActa != null) {
        	detActaTransmitidaDto = new ArrayList<>();
            for (DetActa detalle : detallesActa) {
                detActaTransmitidaDto.add(DetActaPorTransmitirDto
                        .builder()
                        .idActaDetalle(detalle.getId())
                        .idActa(idActa)
                        .idAgrupacionPolitica(detalle.getAgrupacionPolitica() != null ? detalle.getAgrupacionPolitica().getId() : null)
                        .posicion(detalle.getPosicion())
                        .votos(detalle.getVotos())
                        .votosAutomatico(detalle.getVotosAutomatico())
                        .votosManual1(detalle.getVotosManual1())
                        .votosManual2(detalle.getVotosManual2())
                        .estadoErrorMaterial(detalle.getEstadoErrorMaterial())
                        .estado(detalle.getEstado())
                        .ilegible(detalle.getIlegible())
                        .ilegiblev1(detalle.getIlegiblev1())
                        .ilegiblev2(detalle.getIlegiblev2())
                        .detActaPreferencial(mapActaPreferencial(detalle))
                        .detActaOpcion(mapDetallesOpcion(detalle))
                        .activo(detalle.getActivo())
                        .audFechaCreacion(DateUtil.getDateString(detalle.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .audFechaModificacion(DateUtil.getDateString(detalle.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .audUsuarioCreacion(detalle.getUsuarioCreacion())
                        .audUsuarioModificacion(detalle.getUsuarioModificacion())
                        .build());
            }
        }
        return detActaTransmitidaDto;
    }
	
	private List<DetActaAccionPorTransmitirDto> mapDetallesAccion(Long idActa){
		List<DetActaAccionPorTransmitirDto> detActaAccionTransmitidaDto = null;
		List<DetActaAccion> acciones = this.detActaAccionRepository.findByActa_Id(idActa);
		if (acciones != null) {
			detActaAccionTransmitidaDto = new ArrayList<>();
            for (DetActaAccion detalle : acciones) {
            	detActaAccionTransmitidaDto.add(DetActaAccionPorTransmitirDto
                        .builder()
                        .idDetActaAccion(detalle.getId())
                        .idCcDetActaAccion(String.format(CC_FORMATO, this.getCodigoCentroComputoActual(), detalle.getId().toString())) // id orc
                        .idActa(idActa)
                        .accion(detalle.getAccion())
                        .tiempo(detalle.getTiempo())
                        .iteracion(detalle.getIteracion())
                        .orden(detalle.getOrden())
                        .activo(detalle.getActivo())
                        .usuarioAccion(detalle.getUsuarioAccion())
                        .fechaAccion(DateUtil.getDateString(detalle.getFechaAccion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .audFechaCreacion(DateUtil.getDateString(detalle.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .audFechaModificacion(DateUtil.getDateString(detalle.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .audUsuarioCreacion(detalle.getUsuarioCreacion())
                        .audUsuarioModificacion(detalle.getUsuarioModificacion())
                        .build());
            }
        }
        return detActaAccionTransmitidaDto;
	}

	private List<DetActaOpcionPorTransmitirDto> mapDetallesOpcion(DetActa detActa){
		List<DetActaOpcion> detallesActaOpcion = this.detActaOpcionRepository.findByDetActa(detActa);
		List<DetActaOpcionPorTransmitirDto> actasOpcionDto = null;
		if(detallesActaOpcion!=null) {
			actasOpcionDto = detallesActaOpcion
					.stream()
					.map(tp -> DetActaOpcionPorTransmitirDto
			                .builder()
			                .idDetActaOpcion(tp.getId())
			                .idActaDetalle(tp.getDetActa().getId())
			                .idDetActaOpcionCc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), tp.getId().toString())) // id orc
			                .posicion(tp.getPosicion())
			                .votos(tp.getVotos())
			                .votosAutomatico(tp.getVotosAutomatico())
			                .votosManual1(tp.getVotosManual1())
			                .votosManual2(tp.getVotosManual2())
			                .estadoErrorMaterial(tp.getEstadoErrorMaterial())
			                .ilegible(tp.getIlegible())
			                .ilegibleAutomatico(tp.getIlegibleAutomatico())
			                .ilegiblev1(tp.getIlegiblev1())
			                .ilegiblev2(tp.getIlegiblev2())
			                .activo(tp.getActivo())
	                        .audFechaCreacion(DateUtil.getDateString(tp.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
	                        .audFechaModificacion(DateUtil.getDateString(tp.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
	                        .audUsuarioCreacion(tp.getUsuarioCreacion())
	                        .audUsuarioModificacion(tp.getUsuarioModificacion())
			                .build())
					.toList();		
		}
		return actasOpcionDto;
	}
	

    private List<DetActaPreferencialPorTransmitirDto> mapActaPreferencial(DetActa detActa) {
		List<DetActaPreferencial> preferenciales = this.detActaPreferencialRepository.findByDetActa(detActa);
		List<DetActaPreferencialPorTransmitirDto> preferencialesDto = null;
		if(preferenciales!=null) {
			preferencialesDto = preferenciales
					.stream()
					.map(tp -> DetActaPreferencialPorTransmitirDto
			                .builder()
			                .id(tp.getId())
			                .idOrc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), tp.getId().toString()))
			                .idDetalleActa(tp.getDetActa().getId())
			                .idDistritoElectoral(tp.getDistritoElectoral()!=null ? tp.getDistritoElectoral().getId() : null)
			                .posicion(tp.getPosicion())
			                .lista(tp.getLista())
			                .votos(tp.getVotos())
			                .votosAutomatico(tp.getVotosAutomatico())
			                .votosv1(tp.getVotosManual1())
			                .votosv2(tp.getVotosManual2())
			                .estadoErrorMaterial(tp.getEstadoErrorMaterial())
			                .ilegible(tp.getIlegible())
			                .ilegiblev1(tp.getIlegiblev1())
			                .ilegiblev2(tp.getIlegiblev2())
			                .activo(tp.getActivo())
			                .audFechaCreacion(DateUtil.getDateString(tp.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
	                        .audFechaModificacion(DateUtil.getDateString(tp.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
	                        .audUsuarioCreacion(tp.getUsuarioCreacion())
	                        .audUsuarioModificacion(tp.getUsuarioModificacion())
			                .build())
					.toList();		
		}
		return preferencialesDto;

	}

    protected List<DetActaResolucionPorTransmitirDto> mapResoluciones(Long idActa) {
		List<DetActaResolucionPorTransmitirDto> detActaResolucionDto = null;
		List<DetActaResolucion> resoluciones = this.detActaResolucionRepository.findByActa_Id(idActa);
        if (resoluciones != null) {
        	detActaResolucionDto = new ArrayList<>();
            for (DetActaResolucion detalle : resoluciones) {
                detActaResolucionDto.add(DetActaResolucionPorTransmitirDto
                        .builder()
                        .idActa(detalle.getActa().getId())
                        .correlativo(detalle.getCorrelativo())
                        .estadoActa(detalle.getEstadoActa())
                        .activo(detalle.getActivo())
                        .audUsuarioCreacion(detalle.getUsuarioCreacion())
                        .audUsuarioModificacion(detalle.getUsuarioModificacion())
                        .audFechaCreacion(DateUtil.getDateString(detalle.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .audFechaModificacion(DateUtil.getDateString(detalle.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .resolucionDto(ResolucionPorTransmitirDto
                                .builder()
                                .id(detalle.getResolucion().getId())
                                .idCc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), detalle.getResolucion().getId().toString()))
                                .idArchivoResolucion(detalle.getResolucion().getArchivoResolucion()!=null ? detalle.getResolucion().getArchivoResolucion().getId() : null)
                                .fechaResolucion(DateUtil.getDateString(detalle.getResolucion().getFechaResolucion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                                .procedencia(detalle.getResolucion().getProcedencia())
                                .numeroExpediente(detalle.getResolucion().getNumeroExpediente())
                                .numeroResolucion(detalle.getResolucion().getNumeroResolucion())
                                .tipoResolucion(detalle.getResolucion().getTipoResolucion())
                                .estadoResolucion(detalle.getResolucion().getEstadoResolucion())
                                .estadoDigitalizacion(detalle.getResolucion().getEstadoDigitalizacion())
                                .observacionDigitalizacion(detalle.getResolucion().getObservacionDigitalizacion())
                                .numeroPaginas(detalle.getResolucion().getNumeroPaginas())
                                .audUsuarioCreacion(detalle.getResolucion().getAudUsuarioCreacion())
                                .audUsuarioModificacion(detalle.getResolucion().getAudUsuarioModificacion())
                                .audFechaCreacion(DateUtil.getDateString(detalle.getResolucion().getAudFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                                .audFechaModificacion(DateUtil.getDateString(detalle.getResolucion().getAudFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                                .activo(detalle.getResolucion().getActivo())
                                .build())
                        .build());
            }
        }
        
        return detActaResolucionDto;
	}
    
    
    private CabActaFormatoPorTransmitirDto mapCabActaFormatoDtoById(Long id){
    	Optional<CabActaFormato> cabActaFormatoOp = this.cabActaFormatoRepository.findById(id); 
    	if(cabActaFormatoOp.isPresent()){
    		CabActaFormato cabActaFormato = cabActaFormatoOp.get();
    		Archivo archivo = cabActaFormato.getArchivoFormatoPdf();
    		return CabActaFormatoPorTransmitirDto
    				.builder()
    				.id(cabActaFormato.getId())
    				.tabFormato(mapFormatoById(cabActaFormato.getFormato()!=null 
    				? cabActaFormato.getFormato().getId()
    				: null))
    				.idCc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), cabActaFormato.getId().toString()))
    				.correlativo(cabActaFormato.getCorrelativo())
    				.idArchivo(archivo!=null ? archivo.getId() : null)
    				.activo(cabActaFormato.getActivo())
                    .usuarioCreacion(cabActaFormato.getUsuarioCreacion())
                    .usuarioModificacion(cabActaFormato.getUsuarioModificacion())
                    .fechaCreacion(DateUtil.getDateString(cabActaFormato.getFechaCreacion(), 
                    		SceConstantes.FORMATO_FECHA_TRANSMISION))
                    .fechaModificacion(DateUtil.getDateString(cabActaFormato.getFechaModificacion(), 
                    		SceConstantes.FORMATO_FECHA_TRANSMISION))
                    .detalle(mapDetActaFormatoPorTransmitirByIdCabActaFormato(cabActaFormato.getId()))
                    .build();
    	}
    	return null;
    }
    
    private TabFormatoPorTransmitirDto mapFormatoById(Integer id){
    	Optional<Formato> formatoOp = this.tabFormatoRepository.findById(id); 
    	if(formatoOp.isPresent()){
    		Formato formato = formatoOp.get();
    		Archivo archivo = formato.getArchivo();
    		return TabFormatoPorTransmitirDto
    				.builder()
    				.id(formato.getId())
    				.idCc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), formato.getId().toString()))
    				.tipoFormato(formato.getTipoFormato())
    				.correlativo(formato.getCorrelativo())
    				.idArchivo(archivo!=null ? archivo.getId() : null)
    				.activo(formato.getActivo())
                    .usuarioCreacion(formato.getUsuarioCreacion())
                    .usuarioModificacion(formato.getUsuarioModificacion())
                    .fechaCreacion(DateUtil.getDateString(formato.getFechaCreacion(), 
                    		SceConstantes.FORMATO_FECHA_TRANSMISION))
                    .fechaModificacion(DateUtil.getDateString(formato.getFechaModificacion(), 
                    		SceConstantes.FORMATO_FECHA_TRANSMISION))
                    .build();
    	}
    	return null;
    }
    
    private List<DetActaFormatoPorTransmitirDto> mapDetActaFormatoPorTransmitirByIdCabActaFormato(Long id){
    	List<DetActaFormato> detActaFormatos = this.detActaFormatoRepository.findByIdCabActaFormato(id); 
    	List<DetActaFormatoPorTransmitirDto> detalles = null;
    	if(detActaFormatos!=null){
    		detalles = new ArrayList<>();
    		for(DetActaFormato detActaFormato:detActaFormatos){
    			DetActaFormatoPorTransmitirDto x = DetActaFormatoPorTransmitirDto
        				.builder()
        				.id(detActaFormato.getId())
        				.idActa(detActaFormato.getActa().getId())        				
        				.idCc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), detActaFormato.getId().toString()))
        				.activo(detActaFormato.getActivo())
                        .usuarioCreacion(detActaFormato.getUsuarioCreacion())
                        .usuarioModificacion(detActaFormato.getUsuarioModificacion())
                        .fechaCreacion(DateUtil.getDateString(detActaFormato.getFechaCreacion(), 
                        		SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .fechaModificacion(DateUtil.getDateString(detActaFormato.getFechaModificacion(), 
                        		SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .build();
    			if(x!=null){
    				detalles.add(x);
    			}
    			
    		}
    	}
    	return detalles;
    }
    
    private List<DetOficioTransmistirDto> mapOficioByIdActaCeleste(Long idActaCeleste){
    	List<DetOficioTransmistirDto> detOficiosDto = null;
    	List<DetActaOficio> detOficios= this.detOficioRepository.findByActaCeleste_Id(idActaCeleste);
    	if(detOficios!=null){
    		detOficiosDto = new ArrayList<>();
    		for(DetActaOficio detOficio: detOficios){
    			detOficiosDto.add(
    					DetOficioTransmistirDto
    					.builder()
    					.cabActaFormato(mapCabActaFormatoDtoById(detOficio.getCabActaFormato()!=null 
				    					? detOficio.getCabActaFormato().getId()
				    					: null))
    					.idCc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), detOficio.getId().toString()))
    					.idActa(detOficio.getActa().getId())
    					.idActaCelesteCc(String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), detOficio.getActaCeleste().getId().toString()))
    					.cabOficio(
    							OficioPorTransmitirDto
    							.builder()
    							.idCc((String.format(CC_FORMATO,  this.getCodigoCentroComputoActual(), 
    									detOficio.getOficio().getId().toString())))
    							.idArchivo(detOficio.getOficio().getArchivo()!=null 
    								? detOficio.getOficio().getArchivo().getId()
    								: null)
    							.idCentroComputo(detOficio.getOficio().getCentroComputo())
    							.nombreOficio(detOficio.getOficio().getNombreOficio())
    							.estadoOficio(detOficio.getOficio().getEstado())
    							.activo(detOficio.getOficio().getActivo())
    	                        .usuarioCreacion(detOficio.getOficio().getUsuarioCreacion())
    	                        .usuarioModificacion(detOficio.getOficio().getUsuarioModificacion())
    	                        .fechaCreacion(DateUtil.getDateString(detOficio.getOficio().getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
    	                        .fechaModificacion(DateUtil.getDateString(detOficio.getOficio().getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
    	                        .build()
    					)
    					.activo(detOficio.getActivo())
                        .usuarioCreacion(detOficio.getUsuarioCreacion())
                        .usuarioModificacion(detOficio.getUsuarioModificacion())
                        .fechaCreacion(DateUtil.getDateString(detOficio.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .fechaModificacion(DateUtil.getDateString(detOficio.getFechaModificacion(), SceConstantes.FORMATO_FECHA_TRANSMISION))
                        .build()
    				);
    		}
    	}
    	return detOficiosDto;
    }
	
	private void mapCommonFields(TransmisionDto transmision, ActaTransmisionNacion actaTransmitida, String proceso) {
        transmision.setIdTransmision(actaTransmitida.getId());
        transmision.setAcronimoProceso(proceso);
        transmision.setEstadoTransmitidoNacion(actaTransmitida.getEstadoTransmitidoNacion());
        transmision.setFechaRegistro(DateUtil.getDateString(actaTransmitida.getFechaCreacion(), SceConstantes.FORMATO_FECHA_TRANSMISION));
        transmision.setFechaTransmision(DateUtil.getDateString(actaTransmitida.getFechaTransmision(), SceConstantes.FORMATO_FECHA_TRANSMISION));
        transmision.setUsuarioTransmision(actaTransmitida.getUsuarioTransmision());
    }
	
    @Override
    @Transactional(readOnly = true)
    public List<ActaTransmisionNacion> traza(Long idActa) {
        return this.actaTransmisionNacionRepository.listarTrazabilidadPorActaId(idActa);
    }


	@Override
	@Transactional(readOnly = true)
	public List<ActaTransmisionNacion> trazaActaExcluidos(Long idActa) {

		List<String> tiposExcluidos = Arrays.asList(
				TransmisionNacionEnum.REGISTRO_PERSONEROS.name(),
				TransmisionNacionEnum.REGISTRO_MIEMBRO_MESA_ESCRUTINIO.name(),
				TransmisionNacionEnum.OMISOS_MIEMBRO_MESA.name(),
				TransmisionNacionEnum.OMISOS_LISTA_ELECTORES_VOTANTES.name(),
				TransmisionNacionEnum.CC_RECHAZAR_RES_TRANSMISION.name()
		);
		return this.actaTransmisionNacionRepository.listarTrazabilidadPorActaId(idActa, tiposExcluidos);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActaTransmisionNacionTrazabilidadDto> trazaActaConInicioFin(Long idActa) {

		List<String> tiposExcluidos = Arrays.asList(
				TransmisionNacionEnum.REGISTRO_PERSONEROS.name(),
				TransmisionNacionEnum.REGISTRO_MIEMBRO_MESA_ESCRUTINIO.name(),
				TransmisionNacionEnum.OMISOS_MIEMBRO_MESA.name(),
				TransmisionNacionEnum.OMISOS_LISTA_ELECTORES_VOTANTES.name()
		);

		List<Object[]> rawResults = this.actaTransmisionNacionRepository.listarTrazabilidadConFechasInicioFin(idActa, tiposExcluidos);
		List<ActaTransmisionNacionTrazabilidadDto> resultado = new ArrayList<>();

		for (Object[] row : rawResults) {
			try {
				ActaTransmisionNacionTrazabilidadDto dto = new ActaTransmisionNacionTrazabilidadDto();
				dto.setId(((Number) row[0]).longValue());
				dto.setIdActa(((Number) row[1]).longValue());
				dto.setEstadoTransmitidoNacion((Integer) row[2]);
				dto.setTipoTransmision((String) row[3]);
				dto.setTransmite((Integer) row[4]);
				dto.setFechaTransmision((Date) row[5]);
				dto.setFechaRegistro((Date) row[6]);
				dto.setUsuarioRegistro((String) row[7]);
				dto.setAccion((String) row[8]);
				dto.setUsuarioTransmision((String) row[9]);
				dto.setIntento((Integer) row[10]);
				dto.setFechaInicio((Date) row[11]);
				dto.setFechaFin((Date) row[12]);

				if (row[13] instanceof String json) {
					dto.setRequestActaTransmision(objectMapper.readValue(json, TransmisionDto.class));
				} else if (row[13] instanceof Map<?, ?>  map) {
					String json = objectMapper.writeValueAsString(map);
					dto.setRequestActaTransmision(objectMapper.readValue(json, TransmisionDto.class));
				}

				resultado.add(dto);
			} catch (Exception e) {
				throw new InternalServerErrorException(String.format("Error parseando requestActaTransmision %s", e.getMessage()));
			}
		}

		return resultado;

	}

	@Transactional(readOnly = true)
    public List<ActaTransmisionNacion> listarFaltantesTransmitir(Long idActa) {
        return this.actaTransmisionNacionRepository.listarFaltantesTransmitir(
        		idActa, 
        		ConstantesComunes.INACTIVO, 
        		ConstanteAccionTransmision.ACCION_ACTA,
        		ConstanteAccionTransmision.ACCION_MESA_DETALLE);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void actualizarPreTransmision(List<ActaTransmisionNacion> actasTransmistidas, String usuario) {
        if (actasTransmistidas != null) {
        	actasTransmistidas.forEach(
                    transmision -> {
                        Optional<ActaTransmisionNacion> actaTransmisionOp = this.actaTransmisionNacionRepository.findById(transmision.getId());
                        if (actaTransmisionOp.isPresent()) {
                            ActaTransmisionNacion actaTransmision = actaTransmisionOp.get();
                            actaTransmision.setUsuarioTransmision(usuario);
                            actaTransmision.setFechaTransmision(DateUtil.getFechaActualPeruana());
                            this.actaTransmisionNacionRepository.save(actaTransmision);
                        }
                    });
        }

    }
	
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void actualizarPostTransmision(List<ActaTransmitidaDto> actasTransmision) {
        if (actasTransmision != null) {
            actasTransmision.forEach(
                    transmision -> {
                        Optional<ActaTransmisionNacion> actaTransmisionOp = this.actaTransmisionNacionRepository.findById(transmision.getIdTransmision());
                        if (actaTransmisionOp.isPresent()) {
                            ActaTransmisionNacion actaTransmision = actaTransmisionOp.get();
                            actaTransmision.setEstadoTransmitidoNacion(transmision.getEstadoTransmitidoNacion());
                            actaTransmision.setIntento((actaTransmision.getIntento()==null ? 0:actaTransmision.getIntento())+1);
                            this.actaTransmisionNacionRepository.save(actaTransmision);
                        }
                    });
        }

    }
	
    @Override
	public void showJson(boolean show, List<ActaTransmisionNacion> actasTransmistidas) {
		ObjectMapper mappers = new ObjectMapper();
        mappers.enable(SerializationFeature.INDENT_OUTPUT); // Habilitar formato "pretty"
        try {
			String json = mappers.writeValueAsString(actasTransmistidas);
			logger.info("json trasmision {}", json);
		} catch (JsonProcessingException e) {
            logger.error("Se ha producido un error al convertir a JSON: {}", e.getMessage());
		}
	}
	
    @Override
	@Transactional(readOnly = true)
	public List<ActaTransmisionNacion> listarFaltantesTransmitirPorFallo() {
        return this.actaTransmisionNacionRepository.listarFaltantesTransmitirPorFallo(
        		ConstantesComunes.INACTIVO);
    }
    
    @Override
	@Transactional(readOnly = true)
    public Optional<Acta> findByIdActa(Long idActa){
    	return this.actaRepository.findById(idActa);
    }

	private String convertToBase64(String filePath) throws IOException {
		File file = new File(filePath);
		if(file.exists()){
			try (FileInputStream fis = new FileInputStream(file); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				byte[] buf = new byte[1024];
				int bytesRead;
				while ((bytesRead = fis.read(buf)) != -1) {
					bos.write(buf, 0, bytesRead);
				}
				byte[] fileBytes = bos.toByteArray();
				return Base64.getEncoder().encodeToString(fileBytes);
			}
		} else {
			throw new IOException("El archivo no existe");
		}
		
	}
	
	private String determinarAccion(TransmisionNacionEnum estadoEnum){
			String resultado;
		    switch (estadoEnum) {
		    	case OMISOS_LISTA_ELECTORES_VOTANTES,
			         OMISOS_MIEMBRO_MESA,
			         REGISTRO_MIEMBRO_MESA_ESCRUTINIO,
			         REGISTRO_PERSONEROS:
		        	resultado = ConstanteAccionTransmision.ACCION_MESA_DETALLE;
		            break;
		    	case SOBRES_CELESTES:
		    		resultado = ConstanteAccionTransmision.ACCION_SOBRE_CELESTE;
		            break;
		        case PUESTA_CERO:
		        	resultado = ConstanteAccionTransmision.ACCION_PUESTA_CERO;
		            break;
		        default:
		        	resultado = ConstanteAccionTransmision.ACCION_ACTA;
		        	break;
		    }
		    return resultado;
	}
	


	public String getCodigoCentroComputoActual(){
		Optional<CentroComputo> centroComputo = this.centroComputoService.getCentroComputoActual();
	    if (centroComputo.isPresent()) {
	        return centroComputo.get().getCodigo();
	    } else {
	        throw new IllegalStateException("No se encontró el centro de cómputo actual.");
	    }
	}
	
	@Transactional(readOnly = true)
	public boolean hayPendientes(Long idActa){
		List<ActaTransmisionNacion> actas = this.actaTransmisionNacionRepository.listarPendientes(idActa, 
				ConstantesComunes.ESTADO_TRANSMISION_ERROR,
				ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE);
		boolean rpta = false;
		if(actas!=null && !actas.isEmpty()){
			rpta = true;
		}
		return rpta;
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void actualizarEstado(Long idTransmision, Integer estado, Integer intento) {
		Optional<ActaTransmisionNacion> a = this.actaTransmisionNacionRepository.findById(idTransmision);
		if(a.isPresent()){
			ActaTransmisionNacion x = a.get();
			x.setEstadoTransmitidoNacion(estado);
			x.setIntento(intento);
			this.actaTransmisionNacionRepository.save(x);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void actualizarEstado(Long idTransmision, Integer estado) {
		Optional<ActaTransmisionNacion> a = this.actaTransmisionNacionRepository.findById(idTransmision);
		if(a.isPresent()){		
			ActaTransmisionNacion x = a.get();
			x.setEstadoTransmitidoNacion(estado);
			
			if(estado.equals(ConstantesComunes.ESTADO_TRANSMISION_ERROR)){
				int intento = a.get().getIntento()==null 
						? ConstantesComunes.CERO_INTENTO_TRANSMISION
						: a.get().getIntento()+1;	
				x.setIntento(intento);
			}
			
			this.actaTransmisionNacionRepository.save(x);
		}
	}


	@Override
	@Transactional(readOnly = true)
	public List<Long> listarActasNoBloqueadas() {
		return this.actaTransmisionNacionRepository.listarActasNoBloqueadas(ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE);
	}


	@Override
	@Transactional(readOnly = true)
	public int transmisionesBloqueadas(Long idActa) {
		List<ActaTransmisionNacion> x= this.actaTransmisionNacionRepository.listarEjecutandosePorIdActa(idActa, ConstantesComunes.ESTADO_TRANSMISION_EJECUTANDOSE);
		if(x==null){
			return 0;
		}else {
			return x.size();
		}
	}
	
	@Override
	@Transactional(readOnly = true)
    public List<ActaTransmisionNacion> findByIdActaConTransmisionesOrdenadas(Long idActa) {
        return this.actaTransmisionNacionRepository.findByIdActaConTransmisionesOrdenadas(idActa);
    }

}
