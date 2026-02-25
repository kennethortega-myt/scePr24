package pe.gob.onpe.sceorcbackend.model.queue;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import pe.gob.onpe.sceorcbackend.model.dto.queue.*;
import pe.gob.onpe.sceorcbackend.utils.ConstantesComunes;
import pe.gob.onpe.sceorcbackend.utils.ConstantsQueues;


@Service
@Slf4j
public class RabbitMqSender {

    Logger logger = LoggerFactory.getLogger(RabbitMqSender.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNewActa(NewActa message) {
        rabbitTemplate.convertAndSend(ConstantsQueues.EXCHAGE_DIRECT, ConstantsQueues.ROUTING_KEY_NEW_ACTA, message);
    }
    
    public void sendNewActaCeleste(NewActa message) {
        rabbitTemplate.convertAndSend(ConstantsQueues.EXCHAGE_DIRECT, ConstantsQueues.ROUTING_KEY_NEW_ACTA_CELESTE, message);
    }

    public void sendProcessActa(ApprovedActa message) {
        rabbitTemplate.convertAndSend(ConstantsQueues.EXCHAGE_DIRECT, ConstantsQueues.ROUTING_KEY_PROCESS_ACTA, message);
    }

    public void sendProcessLeMm(ApprovedLeMm message) {
        if (message.getAbrevDocumento().equals(ConstantesComunes.ABREV_DOCUMENT_LISTA_ELECTORES))
            rabbitTemplate.convertAndSend(ConstantsQueues.EXCHAGE_DIRECT, ConstantsQueues.ROUTING_KEY_PROCESS_LISTA_ELECTORES, message);
        else if (message.getAbrevDocumento().equals(ConstantesComunes.ABREV_DOCUMENT_HOJA_DE_ASISTENCIA))
            rabbitTemplate.convertAndSend(ConstantsQueues.EXCHAGE_DIRECT, ConstantsQueues.ROUTING_KEY_PROCESS_MIEMBROS_MESA, message);
    }
    
    public void sendProcessActaStae(NewActaStae message) {
        rabbitTemplate.convertAndSend(ConstantsQueues.EXCHAGE_DIRECT, ConstantsQueues.ROUTING_KEY_PROCESS_ACTA_STAE, message);
    }

}