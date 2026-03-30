package com.example.doannhom15.repository;

import com.example.doannhom15.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    
    List<BookingSeat> findByBookingId(Long bookingId);
    
    /** Ghế đã được đặt bởi đơn có trạng thái khác CANCELLED (vé hủy thì ghế hiện lại trống). */
    @Query("SELECT bs.seat.id FROM BookingSeat bs WHERE bs.booking.showtime.id = :showtimeId AND bs.booking.status <> 'CANCELLED'")
    List<Long> findBookedSeatIdsByShowtimeId(Long showtimeId);
    
    boolean existsByBookingIdAndSeatId(Long bookingId, Long seatId);
}
