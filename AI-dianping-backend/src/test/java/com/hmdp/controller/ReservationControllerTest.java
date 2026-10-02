package com.hmdp.controller;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Reservation;
import com.hmdp.service.IReservationService;
import com.hmdp.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationControllerTest {

    private final IReservationService reservationService = mock(IReservationService.class);
    private final ReservationController controller = new ReservationController(reservationService);

    @AfterEach
    void clearUser() {
        UserHolder.removeUser();
    }

    @Test
    void reservationOwnerCanReadReservation() {
        UserHolder.saveUser(user(42L));
        when(reservationService.getById(7L)).thenReturn(reservation(42L));

        assertTrue(controller.getById(7L).getSuccess());
    }

    @Test
    void anotherUserCannotReadReservation() {
        UserHolder.saveUser(user(42L));
        when(reservationService.getById(7L)).thenReturn(reservation(99L));

        assertFalse(controller.getById(7L).getSuccess());
    }

    @Test
    void anonymousUserCannotReadReservation() {
        when(reservationService.getById(7L)).thenReturn(reservation(42L));

        assertFalse(controller.getById(7L).getSuccess());
    }

    private UserDTO user(Long id) {
        UserDTO user = new UserDTO();
        user.setId(id);
        return user;
    }

    private Reservation reservation(Long userId) {
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        return reservation;
    }
}
