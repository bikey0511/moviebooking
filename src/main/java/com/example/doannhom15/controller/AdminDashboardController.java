package com.example.doannhom15.controller;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.model.Movie;
import com.example.doannhom15.model.User;
import com.example.doannhom15.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    
    private final MovieService movieService;
    private final BookingService bookingService;
    private final UserService userService;
    private final ShowtimeService showtimeService;
    private final PaymentService paymentService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalMovies = movieService.count();
        long totalBookings = bookingService.count();
        long totalUsers = userService.count();
        BigDecimal totalRevenue = bookingService.getTotalRevenue();
        
        // Get monthly revenue data
        List<Object[]> monthlyRevenueData = bookingService.getMonthlyRevenue(Calendar.getInstance().get(Calendar.YEAR));
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : monthlyRevenueData) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        
        // Get booking counts by status
        long paidBookings = bookingService.countByStatus(Booking.BookingStatus.PAID);
        long pendingBookings = bookingService.countByStatus(Booking.BookingStatus.PENDING);
        long cancelledBookings = bookingService.countByStatus(Booking.BookingStatus.CANCELLED);
        
        // Get recent bookings
        Page<Booking> recentBookingsPage = bookingService.getAllBookings(0, 5);
        List<Booking> recentBookings = recentBookingsPage.getContent();
        Map<Long, BigDecimal> recentTotalPaymentMap = new HashMap<>();
        recentBookings.forEach(b -> recentTotalPaymentMap.put(b.getId(), paymentService.getTotalAmount(b)));
        
        model.addAttribute("totalMovies", totalMovies);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("monthlyRevenue", revenueMap);
        model.addAttribute("paidBookings", paidBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("totalPaymentMap", recentTotalPaymentMap);
        
        return "admin/dashboard";
    }
    
    @GetMapping("/dashboard-data")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        long totalMovies = movieService.count();
        long totalBookings = bookingService.count();
        long totalUsers = userService.count();
        BigDecimal totalRevenue = bookingService.getTotalRevenue();
        
        data.put("totalMovies", totalMovies);
        data.put("totalBookings", totalBookings);
        data.put("totalUsers", totalUsers);
        data.put("totalRevenue", totalRevenue);
        
        List<Object[]> monthlyRevenueData = bookingService.getMonthlyRevenue(Calendar.getInstance().get(Calendar.YEAR));
        List<String> months = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        
        for (int i = 1; i <= 12; i++) {
            months.add("Month " + i);
            final int month = i;
            double revenue = monthlyRevenueData.stream()
                    .filter(row -> ((Number) row[0]).intValue() == month)
                    .mapToDouble(row -> ((Number) row[1]).doubleValue())
                    .findFirst()
                    .orElse(0.0);
            revenues.add(revenue);
        }
        
        data.put("months", months);
        data.put("revenues", revenues);
        
        return data;
    }
    
    @GetMapping("/analytics")
    public String analytics(Model model) {
        List<Object[]> monthlyRevenueData = bookingService.getMonthlyRevenue(Calendar.getInstance().get(Calendar.YEAR));
        
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : monthlyRevenueData) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        
        model.addAttribute("monthlyRevenue", revenueMap);
        
        return "admin/analytics";
    }
}
