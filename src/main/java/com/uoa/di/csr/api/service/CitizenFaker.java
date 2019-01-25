package com.uoa.di.csr.api.service;

import com.github.javafaker.Faker;
import com.uoa.di.csr.api.domain.base.Citizen;
import com.uoa.di.csr.api.domain.base.ServiceRequest;
import com.uoa.di.csr.api.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CitizenFaker {

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private CitizenRepository citizenRepository;

    public void populateCitizens() {
        Faker faker = new Faker();
        List<ServiceRequest> serviceRequests = serviceRequestService.findAll().stream().peek(serviceRequest -> serviceRequest.getUpvotersIds().clear()).collect(Collectors.toList());
        serviceRequestService.saveServiceRequests(serviceRequests);
        int citizensToGenerate = serviceRequests.size() / 2;
        int upvotesLimit = 5;
        List<Citizen> citizens = IntStream
                .range(0, citizensToGenerate)
                .boxed()
                .map(integer -> {
                    Citizen citizen = new Citizen();
                    citizen.setAddress(faker.address().fullAddress());
                    citizen.setTelephoneNumber(faker.phoneNumber().phoneNumber());
                    citizen.setFirstName(faker.name().firstName());
                    citizen.setLastName(faker.name().lastName());
                    return citizen;
                })
                .collect(Collectors.toList());
        citizenRepository.saveAll(citizens);
        List<Citizen> mongoCitizens = citizenRepository.findAll();
        mongoCitizens.forEach(citizen -> {
            int serviceRequestsToUpvote = ThreadLocalRandom.current().nextInt(upvotesLimit);
            IntStream
                    .range(0, serviceRequestsToUpvote)
                    .boxed()
                    .forEach(upvote -> {
                        ServiceRequest chosen = serviceRequests.get(ThreadLocalRandom.current().nextInt(serviceRequests.size()));
                        try {
                            System.out.println(String.format("Upvote: %s", serviceRequestService.upvoteServiceRequest(citizen, chosen.getSrId())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        });
    }
}
