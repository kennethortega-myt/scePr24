package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pe.gob.onpe.sceorcbackend.model.enums.TransmisionNacionEnum;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionHttpService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionStrategyService;

@Service
public class ActaTransmisionNacionStrategyServiceImpl implements ActaTransmisionNacionStrategyService {

	@Value("${app.orc.transmision.mq}")
    private boolean habilitarKafka;
	
	private final ActaTransmisionNacionHttpService actaTransmisionNacionHttpService;
	
	public ActaTransmisionNacionStrategyServiceImpl(
			ActaTransmisionNacionHttpService actaTransmisionNacionHttpService
    		) {
		this.actaTransmisionNacionHttpService = actaTransmisionNacionHttpService;
	}
	
	@Override
	public void sincronizar(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario) {
		if(!habilitarKafka) {
			this.actaTransmisionNacionHttpService.sincronizar(idActa, proceso, estadoEnum, usuario);
		}
	}
	
	@Override
	public void sincronizar(List<Long> idActas, String proceso, TransmisionNacionEnum estadoEnum, String usuario) {
		if(!habilitarKafka) {
			this.actaTransmisionNacionHttpService.sincronizar(idActas, proceso, estadoEnum, usuario);
		}
	}
	
	@Override
	public void sincronizarSync(Long idActa, String proceso, TransmisionNacionEnum estadoEnum, String usuario) {
		if(habilitarKafka) {
			this.actaTransmisionNacionHttpService.sincronizarSync(idActa, proceso, estadoEnum, usuario);
		}
	}

	@Override
	public void transmitirActa(Long idActa, String proceso, String usuarioTransmision) {
		if(!habilitarKafka) {
			this.actaTransmisionNacionHttpService.tramsmitirActa(idActa, proceso, usuarioTransmision);
		}
	}

}
