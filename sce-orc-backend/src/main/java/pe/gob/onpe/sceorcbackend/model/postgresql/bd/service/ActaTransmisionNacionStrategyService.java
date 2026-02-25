package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service;

import java.util.List;

import pe.gob.onpe.sceorcbackend.model.enums.TransmisionNacionEnum;

public interface ActaTransmisionNacionStrategyService {

	void sincronizar(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario);
	void transmitirActa(Long idActa, String proceso, String usuarioTransmision);
	void sincronizar(List<Long> idActas, String proceso, TransmisionNacionEnum estadoEnum, String usuario);
	void sincronizarSync(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario); 
	
}
