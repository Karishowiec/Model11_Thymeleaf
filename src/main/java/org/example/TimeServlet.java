package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private TemplateEngine templateEngine;

    @Override
    public void init() {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(this.getServletContext());
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCacheable(false);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String timezone = request.getParameter("timezone");
        String lastTimezone = getLastTimezoneFromCookies(request.getCookies());

        ZoneId zoneId;

        if (timezone != null && !timezone.isEmpty()) {
            try {
                zoneId = ZoneId.of(timezone);
                saveTimezoneINCookie(response, timezone);
            } catch (Exception e) {
                zoneId =(lastTimezone != null) ? ZoneId.of(lastTimezone) : ZoneId.of("UTC");
            }
        } else {
            zoneId = (lastTimezone != null) ? ZoneId.of(lastTimezone) : ZoneId.of("UTC");
        }

        ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
        String formattedTime = currentTime.format(formatter);

        WebContext ctx = new WebContext(request, response, getServletContext());
        ctx.setVariable("currentTime", formattedTime);
        ctx.setVariable("timezone", zoneId.toString());

        templateEngine.process("time", ctx, response.getWriter());
    }
    private void saveTimezoneINCookie(HttpServletResponse response, String timezone) {
        Cookie cookie = new Cookie("lastTimezone", timezone);
        cookie.setMaxAge(60 * 60 * 24 * 30);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getLastTimezoneFromCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}