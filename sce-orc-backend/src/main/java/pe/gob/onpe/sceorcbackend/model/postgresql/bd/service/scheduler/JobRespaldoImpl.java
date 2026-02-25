package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import pe.gob.onpe.sceorcbackend.model.dto.ParametroDto;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.CentroComputo;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ProcesoElectoral;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.CentroComputoService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.MaeProcesoElectoralService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ParametroService;
import pe.gob.onpe.sceorcbackend.model.service.RespaldoService;
import pe.gob.onpe.sceorcbackend.rest.controller.RespaldoController;
import pe.gob.onpe.sceorcbackend.utils.ConstantesParametros;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class JobRespaldoImpl implements SchedulingConfigurer {

    Logger log = LoggerFactory.getLogger(RespaldoController.class);

    private final RespaldoService respaldoService;
    private final MaeProcesoElectoralService maeProcesoElectoralService;
    private final CentroComputoService centroComputoService;
    private final ParametroService parametroService;

    public JobRespaldoImpl(RespaldoService respaldoService, MaeProcesoElectoralService maeProcesoElectoralService, CentroComputoService centroComputoService, ParametroService parametroService) {
        this.respaldoService = respaldoService;
        this.maeProcesoElectoralService = maeProcesoElectoralService;
        this.centroComputoService = centroComputoService;
        this.parametroService = parametroService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(schedulerRespaldoExecutor());

        taskRegistrar.addTriggerTask(
                this::ejecutarBackup,
                triggerContext -> {
                    int minutos = 5;
                    ParametroDto parametroDto = parametroService.obtenerParametro(ConstantesParametros.CAB_PARAM_HORA_FRECUENCIA_RESPALDO);
                    if(parametroDto !=null && parametroDto.getValor()!=null){
                        minutos = ((Double) parametroDto.getValor()).intValue();
                    }
                    if (minutos <= 0) {
                        log.info("Scheduler de backup deshabilitado (minutos = 0)");
                        return null;
                    }
                    long delay = TimeUnit.MINUTES.toMillis(minutos);
                    return Instant.ofEpochMilli(System.currentTimeMillis() + delay);
                }
        );
    }

    @Bean(destroyMethod = "shutdown", name = "schedulerRespaldoExecutor")
    public ScheduledExecutorService schedulerRespaldoExecutor() {
        return Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "scheduler-respaldo")
        );
    }

    private void ejecutarBackup() {

        log.info("Se ejecutó el generar backup");

        try {
            String proceso = "";
            String centroComputo = "";
            ProcesoElectoral procesoElectoral = this.maeProcesoElectoralService.findByActivo();
            Optional<CentroComputo> cc = this.centroComputoService.getCentroComputoActual();
            if (Objects.nonNull(procesoElectoral) && cc.isPresent()) {
                    proceso = procesoElectoral.getAcronimo();
                    centroComputo = cc.get().getCodigo();
            }
                this.respaldoService.backupDatabaseAutomatico(proceso, centroComputo, "SISTEMA");

                log.info("Generar backup automático finalizado correctamente");
        } catch (Exception ex) {
            log.error("Error en el job Generar backup automático", ex);
        }
    }

}
