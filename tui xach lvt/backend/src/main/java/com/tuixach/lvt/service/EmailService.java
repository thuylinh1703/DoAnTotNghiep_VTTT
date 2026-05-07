package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.OrderConfirmationMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    /**
     * Gửi email OTP xác nhận đăng ký
     */
    public void sendRegistrationOtpEmail(String toEmail, String fullName, String otpCode) {
        String subject = "Xác nhận đăng ký tài khoản - Túi Xách LVT";

        String htmlBody = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1 style="color: white; margin: 0; font-size: 24px;">🛍️ Túi Xách LVT</h1>
                        <p style="color: rgba(255,255,255,0.9); margin-top: 5px;">Xác nhận đăng ký tài khoản</p>
                    </div>
                    <div style="background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 10px 10px;">
                        <p style="font-size: 16px;">Xin chào <strong>%s</strong>,</p>
                        <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>Túi Xách LVT</strong>.</p>
                        <p>Mã xác nhận của bạn là:</p>
                        <div style="background: #f8f9fa; border: 2px dashed #667eea; border-radius: 10px; padding: 20px; text-align: center; margin: 20px 0;">
                            <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #667eea;">%s</span>
                        </div>
                        <p style="color: #e74c3c; font-size: 14px;">⏰ Mã có hiệu lực trong <strong>5 phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #888; font-size: 12px; text-align: center;">Nếu bạn không yêu cầu đăng ký, vui lòng bỏ qua email này.</p>
                    </div>
                </div>
                """
                .formatted(fullName, otpCode);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    /**
     * Gửi email xác nhận đặt hàng thành công
     */
    public void sendOrderConfirmationEmail(OrderConfirmationMessage message) {
        String subject = "Xác nhận đơn hàng #" + message.getOrderCode() + " - Túi Xách LVT";

        NumberFormat currencyFormat = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

        StringBuilder itemsHtml = new StringBuilder();
        for (OrderConfirmationMessage.OrderItemInfo item : message.getItems()) {
            itemsHtml.append(String.format("""
                    <tr>
                        <td style="padding: 10px; border-bottom: 1px solid #eee;">%s</td>
                        <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: center;">%d</td>
                        <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">%sđ</td>
                        <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">%sđ</td>
                    </tr>
                    """,
                    item.getProductName(),
                    item.getQuantity(),
                    currencyFormat.format(item.getPrice()),
                    currencyFormat.format(item.getSubtotal())));
        }

        String paymentMethodVn = message.getPaymentMethod().equals("COD")
                ? "Thanh toán khi nhận hàng (COD)"
                : "Chuyển khoản ngân hàng";

        String htmlBody = String.format(
                """
                        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                            <div style="background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                                <h1 style="color: white; margin: 0; font-size: 24px;">✅ Đặt hàng thành công!</h1>
                                <p style="color: rgba(255,255,255,0.9); margin-top: 5px;">Mã đơn hàng: <strong>%s</strong></p>
                            </div>
                            <div style="background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 10px 10px;">
                                <p style="font-size: 16px;">Xin chào <strong>%s</strong>,</p>
                                <p>Đơn hàng của bạn đã được đặt thành công. Chi tiết đơn hàng:</p>

                                <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0;">
                                    <p style="margin: 5px 0;"><strong>👤 Người nhận:</strong> %s</p>
                                    <p style="margin: 5px 0;"><strong>📞 Số điện thoại:</strong> %s</p>
                                    <p style="margin: 5px 0;"><strong>📍 Địa chỉ:</strong> %s</p>
                                    <p style="margin: 5px 0;"><strong>💳 Thanh toán:</strong> %s</p>
                                </div>

                                <table style="width: 100%%; border-collapse: collapse; margin: 15px 0;">
                                    <thead>
                                        <tr style="background: #667eea; color: white;">
                                            <th style="padding: 10px; text-align: left;">Sản phẩm</th>
                                            <th style="padding: 10px; text-align: center;">SL</th>
                                            <th style="padding: 10px; text-align: right;">Đơn giá</th>
                                            <th style="padding: 10px; text-align: right;">Thành tiền</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        %s
                                    </tbody>
                                </table>

                                <div style="background: #667eea; color: white; padding: 15px; border-radius: 8px; text-align: right; font-size: 18px;">
                                    <strong>Tổng cộng: %sđ</strong>
                                </div>

                                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                                <p style="color: #888; font-size: 12px; text-align: center;">Cảm ơn bạn đã mua hàng tại Túi Xách LVT! 🛍️</p>
                            </div>
                        </div>
                        """,
                message.getOrderCode(),
                message.getFullName(),
                message.getReceiverName(),
                message.getReceiverPhone(),
                message.getReceiverAddress(),
                paymentMethodVn,
                itemsHtml.toString(),
                currencyFormat.format(message.getTotalAmount()));

        sendHtmlEmail(message.getEmail(), subject, htmlBody);
    }

    /**
     * Gửi email HTML
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Lỗi hệ thống khi gửi email");
        }
    }
}
