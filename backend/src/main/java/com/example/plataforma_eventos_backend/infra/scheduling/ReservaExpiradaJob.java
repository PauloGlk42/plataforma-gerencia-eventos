package com.example.plataforma_eventos_backend.infra.scheduling;

import com.example.plataforma_eventos_backend.services.BookingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Varredura periódica de pedidos PENDENTE vencidos, delegando ao BookingService para
 * devolver o estoque. Rede de segurança para quem não consultar o pedido antes do prazo —
 * a checagem preguiçosa na leitura (GET) já expira na hora, sem depender deste job.
 */
@Component
public class ReservaExpiradaJob {

    private final BookingService bookingService;

    public ReservaExpiradaJob(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelay = 30_000)
    public void expirarPendentesVencidos() {
        bookingService.expirarVencidos();
    }
}
