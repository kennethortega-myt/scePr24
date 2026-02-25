package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service;


import pe.gob.onpe.sceorcbackend.model.dto.TransmisionCreated;
import pe.gob.onpe.sceorcbackend.model.dto.transmision.ActaTransmitidaDto;
import pe.gob.onpe.sceorcbackend.model.dto.trazabilidad.ActaTransmisionNacionTrazabilidadDto;
import pe.gob.onpe.sceorcbackend.model.enums.TransmisionNacionEnum;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Acta;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ActaTransmisionNacion;
import pe.gob.onpe.sceorcbackend.model.postgresql.dto.transmision.TransmisionReqDto;

import java.util.List;
import java.util.Optional;

public interface ActaTransmisionNacionService {

	Optional<ActaTransmisionNacion> findById(Long idTransmision);
	TransmisionCreated guardarTransmision(
			Long idActa, 
			TransmisionNacionEnum estadoEnum, 
			String usuario, 
			String proceso, 
			Integer estadoTransmision,
			Integer intento);
	
	List<ActaTransmisionNacion> traza(Long idActa);

	List<ActaTransmisionNacion> trazaActaExcluidos(Long idActa);

	List<ActaTransmisionNacionTrazabilidadDto> trazaActaConInicioFin(Long idActa);
	void actualizarPostTransmision(List<ActaTransmitidaDto> actasTransmision);
	void actualizarPreTransmision(List<ActaTransmisionNacion> actasTransmistidas, String usuario); 
	List<TransmisionReqDto> adjuntar(List<TransmisionReqDto> actasTransmistidas);
	List<TransmisionReqDto> mapperRequest(List<ActaTransmisionNacion> actasTransmistidas);
	List<ActaTransmisionNacion> listarFaltantesTransmitir(Long idActa); 
	void showJson(boolean show, List<ActaTransmisionNacion> actasTransmistidas);
	List<ActaTransmisionNacion> listarFaltantesTransmitirPorFallo();
	Optional<Acta> findByIdActa(Long idActa);
	boolean hayPendientes(Long idActa);
	void actualizarEstado(Long idTransmision, Integer estado, Integer intento);
	void actualizarEstado(Long idTransmision, Integer estado);
	List<Long> listarActasNoBloqueadas();
	int transmisionesBloqueadas(Long idActa);
	List<ActaTransmisionNacion> findByIdActaConTransmisionesOrdenadas(Long idActa);
	String getCodigoCentroComputoActual();
	
}
