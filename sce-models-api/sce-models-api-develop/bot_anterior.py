import sys
import pika
import config
import json
from modules import digitization, control
import traceback
import time

def main():
    while True:
        try:
            # Establecer la conexión
            connection = pika.BlockingConnection(
                pika.ConnectionParameters(
                    host=config.RABBITMQ_HOST,
                    port=config.RABBITMQ_PORT,
                    credentials=pika.PlainCredentials(
                        username=config.RABBITMQ_USER,
                        password=config.RABBITMQ_PASSWORD
                    ),
                    connection_attempts=10,
                    retry_delay=2,
                    blocked_connection_timeout=30,
                    heartbeat=600
                )
            )

            channel = connection.channel()

            new_actas_queue = "sce-queue-new-acta"
            process_actas_queue = "sce-queue-process-acta"
            process_lista_electores_queue = "sce-queue-process-lista-electores"
            process_miembros_mesa_queue = "sce-queue-process-miembros_mesa"

            channel.basic_qos(prefetch_count=1)

            channel.queue_declare(new_actas_queue, durable=True)
            channel.queue_declare(process_actas_queue, durable=True)

            # declaracion de DIGITALIZACION DE ACTAS
            channel.queue_declare(queue=f"{new_actas_queue}-dlq", durable=True)
            channel.queue_bind(exchange='amq.direct', queue=f"{new_actas_queue}-dlq", routing_key=f"{new_actas_queue}-dlq-rt")

            # declaracion PROCESAMIENTO DE CORTES
            channel.queue_declare(queue=f"{process_actas_queue}-dlq", durable=True)
            channel.queue_bind(exchange='amq.direct', queue=f"{process_actas_queue}-dlq", routing_key=f"{process_actas_queue}-dlq-rt")


            #declaracion de cola LE
            channel.queue_declare(queue=f"{process_lista_electores_queue}", durable=True)
            channel.queue_bind(exchange='amq.direct', queue=f"{process_lista_electores_queue}", routing_key=f"{process_lista_electores_queue}-rt")

            #declaracion de cole LE para manejar errores   -  Dead Letter Queue (DLQ)
            channel.queue_declare(queue=f"{process_lista_electores_queue}-dlq", durable=True)
            channel.queue_bind(exchange='amq.direct', queue=f"{process_lista_electores_queue}-dlq", routing_key=f"{process_lista_electores_queue}-dlq-rt")


            #declaracion de cola MM
            channel.queue_declare(queue=f"{process_miembros_mesa_queue}", durable=True)
            channel.queue_bind(exchange='amq.direct', queue=f"{process_miembros_mesa_queue}", routing_key=f"{process_miembros_mesa_queue}-rt")

            #declaracion de cole MM para manejar errores   -  Dead Letter Queue (DLQ)
            channel.queue_declare(queue=f"{process_miembros_mesa_queue}-dlq", durable=True)
            channel.queue_bind(exchange='amq.direct', queue=f"{process_miembros_mesa_queue}-dlq", routing_key=f"{process_miembros_mesa_queue}-dlq-rt")


            def new_acta_callback(ch, method, properties, body):
                body = json.loads(body)
                try:
                    digitization.validate_mesa(body['actaId'], body['fileId'], body['type'],body['usuario'],body['codigocc'],body['abrevProceso'])
                except Exception as e:
                    print(f"Error processing {body}", file=sys.stderr)
                    traceback.print_exc()

                    # En caso de error enviar a la deal letter queue
                    channel.basic_publish(
                        exchange='amq.direct',
                        routing_key=f"{new_actas_queue}-dlq-rt",
                        body=json.dumps(body).encode('utf-8'),
                        properties=pika.BasicProperties(
                            delivery_mode=2,
                            headers=properties.headers
                        )
                    )


            def process_acta_callback(ch, method, properties, body):
                body = json.loads(body)

                try:
                    #control.process_acta(body['actaId'], body['fileId1'], body['fileId2'])
                    control.process_acta(body['actaId'], body['fileId1'], body['fileId2'], body['codUsuario'], body['codCentroComputo'])

                    # Si todo sale bien, confirmamos el mensaje.
                    # ch.basic_ack(delivery_tag=method.delivery_tag)
                except Exception as e:
                    print(f"Error processing {body}", file=sys.stderr)
                    traceback.print_exc()

                    # En caso de error enviar a la deal letter queue
                    channel.basic_publish(
                        exchange='amq.direct',
                        routing_key=f"{process_actas_queue}-dlq-rt",
                        body=json.dumps(body).encode('utf-8'),
                        properties=pika.BasicProperties(
                            delivery_mode=2,
                            headers=properties.headers
                        )
                    )


            def process_lista_electores_callback(ch, method, properties, body):
                body = json.loads(body)

                try:
                    control.process_lista_electores(body['mesaId'], body['abrevDocumento'], body['codUsuario'], body['codCentroComputo'])
                except Exception as e:
                    print(f"Error processing {body}", file=sys.stderr)
                    traceback.print_exc()

                    # En caso de error enviar a la deal letter queue
                    channel.basic_publish(
                        exchange='amq.direct',
                        routing_key=f"{process_lista_electores_queue}-dlq-rt",
                        body=json.dumps(body).encode('utf-8'),
                        properties=pika.BasicProperties(
                            delivery_mode=2,
                            headers=properties.headers
                        )
                    )


            def process_miembros_mesa_callback(ch, method, properties, body):
                body = json.loads(body)

                try:
                    control.process_miembros_mesa(body['mesaId'], body['abrevDocumento'], body['codUsuario'], body['codCentroComputo'])
                except Exception as e:
                    print(f"Error processing {body}", file=sys.stderr)
                    traceback.print_exc()

                    # En caso de error enviar a la deal letter queue
                    channel.basic_publish(
                        exchange='amq.direct',
                        routing_key=f"{process_miembros_mesa_queue}-dlq-rt",
                        body=json.dumps(body).encode('utf-8'),
                        properties=pika.BasicProperties(
                            delivery_mode=2,
                            headers=properties.headers
                        )
                    )


            # Decirle a RabbitMQ que esta función va a recibir los mensajes de la cola
            channel.basic_consume(queue=new_actas_queue, on_message_callback=new_acta_callback, auto_ack=True)
            channel.basic_consume(queue=process_actas_queue, on_message_callback=process_acta_callback, auto_ack=True)
            channel.basic_consume(queue=process_lista_electores_queue, on_message_callback=process_lista_electores_callback, auto_ack=True)
            channel.basic_consume(queue=process_miembros_mesa_queue, on_message_callback=process_miembros_mesa_callback, auto_ack=True)


            print(' [*] Waiting for messages. To exit press CTRL+C')
            channel.start_consuming()

        except KeyboardInterrupt:
            print("Shutting down...")
            try:
                channel.close()
                connection.close()
            except:
                pass
            sys.exit(0)
            
        except Exception as e:
            print(f"Error en la conexión: {str(e)}", file=sys.stderr)
            traceback.print_exc()
            print("Reintentando conexión en 5 segundos...")
            time.sleep(5)
            continue

if __name__ == "__main__":
    main()
