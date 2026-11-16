package com.invengest.notification.infrastructure.driver_adapters.email;

import com.invengest.notification.domain.model.MensajeCorreo;
import com.invengest.notification.domain.model.gateway.CorreoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SpringMailCorreoGatewayImpl implements CorreoGateway {

    private static final Logger log = LoggerFactory.getLogger(SpringMailCorreoGatewayImpl.class);

    private final JavaMailSender javaMailSender;
    private final String fromEmail;

    public SpringMailCorreoGatewayImpl(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.from:${spring.mail.username:}}") String fromEmail) {
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

    @Async("notificationMailExecutor")
    @Override
    public void enviarCorreo(MensajeCorreo mensaje) {
        try {
            log.info(
                    "Despachando correo asíncrono idSolicitud={} tipo={} destinatarios={}",
                    mensaje.idSolicitud(),
                    mensaje.tipoNotificacion(),
                    mensaje.destinatarios()
            );

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isBlank()) {
                mailMessage.setFrom(fromEmail.trim());
            }
            mailMessage.setTo(mensaje.destinatarios().toArray(String[]::new));
            mailMessage.setSubject(mensaje.asunto());
            mailMessage.setText(mensaje.cuerpo());

            javaMailSender.send(mailMessage);

            log.info(
                    "Correo enviado exitosamente idSolicitud={} tipo={}",
                    mensaje.idSolicitud(),
                    mensaje.tipoNotificacion()
            );
        } catch (MailException ex) {
            log.error(
                    "Error SMTP al enviar correo idSolicitud={} tipo={} causa={}",
                    mensaje.idSolicitud(),
                    mensaje.tipoNotificacion(),
                    ex.getMessage()
            );
        } catch (Exception ex) {
            log.error(
                    "Error inesperado al enviar correo idSolicitud={} tipo={} causa={}",
                    mensaje.idSolicitud(),
                    mensaje.tipoNotificacion(),
                    ex.getMessage()
            );
        }
    }
}
