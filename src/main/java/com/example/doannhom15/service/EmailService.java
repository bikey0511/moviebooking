package com.example.doannhom15.service;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.model.User;
import com.example.doannhom15.service.BookingService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final BookingService bookingService;

    @Async
    public void sendTicketEmail(Long bookingId) {
        try {
            Booking booking = bookingService.getBookingByIdForEmail(bookingId);
            if (booking == null) {
                log.warn("Cannot send email: booking not found {}", bookingId);
                return;
            }
            User user = booking.getUser();
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Cannot send email: user or email is null for booking {}", bookingId);
                return;
            }

            String to = user.getEmail();
            String subject = "🎬 Xác nhận đặt vé xem phim - " + booking.getTicketCode();

            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("user", user);
            context.setVariable("movie", booking.getShowtime().getMovie());
            context.setVariable("showtime", booking.getShowtime());
            context.setVariable("room", booking.getShowtime().getRoom());
            context.setVariable("seats", booking.getBookingSeats());
            context.setVariable("totalPrice", booking.getTotalPrice());
            context.setVariable("concessionTotal", booking.getConcessionTotal());
            context.setVariable("discountAmount", booking.getDiscountAmount());
            context.setVariable("voucherCode", booking.getVoucherCode());
            context.setVariable("concessionVoucherCode", booking.getConcessionVoucherCode());

            // Tính tổng thanh toán
            BigDecimal ticketPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal concession = booking.getConcessionTotal() != null ? booking.getConcessionTotal() : BigDecimal.ZERO;
            BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal totalPayment = ticketPrice.add(concession).subtract(discount).max(BigDecimal.ZERO);
            context.setVariable("totalPayment", totalPayment);

            String htmlContent = templateEngine.process("email/ticket-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("Cinema <tongphat33@gmail.com>");

            mailSender.send(message);
            log.info("Sent ticket email to {} for booking {}", to, booking.getTicketCode());
        } catch (Exception e) {
            log.error("Failed to send ticket email for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    /** Gửi email xác nhận thanh toán thành công (sau khi user chuyển khoản). */
    @Async
    public void sendPaymentConfirmationEmail(Long bookingId) {
        try {
            Booking booking = bookingService.getBookingByIdForEmail(bookingId);
            if (booking == null) {
                log.warn("Cannot send email: booking not found {}", bookingId);
                return;
            }
            User user = booking.getUser();
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Cannot send email: user or email is null for booking {}", bookingId);
                return;
            }

            String to = user.getEmail();
            String subject = "✅ Xác nhận thanh toán thành công - " + booking.getTicketCode();

            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("user", user);
            context.setVariable("movie", booking.getShowtime().getMovie());
            context.setVariable("showtime", booking.getShowtime());
            context.setVariable("room", booking.getShowtime().getRoom());
            context.setVariable("seats", booking.getBookingSeats());

            BigDecimal ticketPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal concession = booking.getConcessionTotal() != null ? booking.getConcessionTotal() : BigDecimal.ZERO;
            BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal totalPayment = ticketPrice.add(concession).subtract(discount).max(BigDecimal.ZERO);
            context.setVariable("totalPayment", totalPayment);

            String htmlContent = templateEngine.process("email/payment-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("Cinema <tongphat33@gmail.com>");

            mailSender.send(message);
            log.info("Sent payment confirmation email to {} for booking {}", to, booking.getTicketCode());
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }
}
