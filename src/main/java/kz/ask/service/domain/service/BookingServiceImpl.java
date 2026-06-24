package kz.ask.service.domain.service;

import java.time.Instant;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.service.domain.entity.Booking;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import kz.ask.service.domain.enums.BookingSource;
import kz.ask.service.domain.enums.BookingStatus;
import kz.ask.service.infrastructure.repository.BookingRepository;
import kz.ask.service.infrastructure.repository.ServiceBranchOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceBranchOfferRepository serviceBranchOfferRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public void createBookingFromRequest(UUID customerId, UUID serviceBranchOfferId,
                                         UUID branchId, Instant requestedStartAt,
                                         Instant confirmedStartAt, Instant confirmedEndAt) {
        AppUser customer = appUserRepository.getReferenceById(customerId);
        ServiceBranchOffer sbo = serviceBranchOfferRepository.getReferenceById(serviceBranchOfferId);
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setServiceBranchOffer(sbo);
        booking.setBranch(branch);
        booking.setStatus(BookingStatus.CONFIRMED_BY_BUSINESS);
        booking.setSource(BookingSource.CUSTOMER_REQUEST);
        booking.setRequestedStartAt(requestedStartAt);
        booking.setConfirmedStartAt(confirmedStartAt);
        booking.setConfirmedEndAt(confirmedEndAt);
        bookingRepository.save(booking);
    }
}
