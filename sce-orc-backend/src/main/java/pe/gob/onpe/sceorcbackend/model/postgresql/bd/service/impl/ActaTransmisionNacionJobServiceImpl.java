package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionHttpService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionMqService;
import pe.gob.onpe.sceorcbackend.utils.DateUtil;

@Component
public class ActaTransmisionNacionJobServiceImpl {

	Logger logger = LoggerFactory.getLogger(ActaTransmisionNacionJobServiceImpl.class);
	
	@Value("${app.orc.transmision.mq}")
    private boolean habilitarKafka;
	
	@Value("${app.orc.transmision.data.enabled}")
    private boolean habilitarJob;
	
	private final ActaTransmisionNacionHttpService actaTransmisionNacionHttpService;
	
	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	public ActaTransmisionNacionJobServiceImpl(
			ActaTransmisionNacionHttpService actaTransmisionNacionHttpService) {
		this.actaTransmisionNacionHttpService = actaTransmisionNacionHttpService;
	}
	
	
	
	@Scheduled(cron = "${app.orc.transmision.data.cron.expression}")
	public void scheduleTaskWithCronExpression() {

		logger.info("Se ejecuto el job para transmitir actas y puestas cero a las {}", DateUtil.getFechaActualPeruana());
		
		if (!isRunning.compareAndSet(false, true)) {
            logger.warn("El job para firmar un documento de instalacion sufragio aún se está ejecutando. Se omite esta ejecución.");
            return;
        }
		
		try {
			if(habilitarJob){
				logger.info("El job se encuentra habilitado");
				if(habilitarKafka) {
					logger.info("Se da inicio la transmision por kafka");
				} else {
					logger.info("Se da inicio la transmision por http");
					this.actaTransmisionNacionHttpService.procesarReintentos();
				}
			} else {
				logger.info("El job no se encuentra habilitado");
			}
		} catch (Exception e) {
            logger.error("Se generó un error en la ejecución del job de transmision", e);
        } finally {
            isRunning.set(false);
        }
		
	}
	
}
