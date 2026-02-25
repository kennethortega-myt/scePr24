package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service;



import java.util.List;

import pe.gob.onpe.sceorcbackend.model.enums.TransmisionNacionEnum;


public interface ActaTransmisionNacionHttpService {

	public void sincronizar(List<Long> idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario);
	void sincronizar(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario);
	void tramsmitirActa(Long idActa, String proceso, String usuario);
	void sincronizarSync(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario);
	void procesarReintentos();
	
}
