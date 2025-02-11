package examen.fapte_bune.service;

import examen.fapte_bune.domain.Nevoie;
import examen.fapte_bune.domain.Oras;
import examen.fapte_bune.domain.Persoana;
import examen.fapte_bune.domain.Status;
import examen.fapte_bune.repository.NevoiRepo;
import examen.fapte_bune.repository.PersoaneRepo;
import examen.fapte_bune.util.Observable;
import examen.fapte_bune.util.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Service implements Observable {
    private final PersoaneRepo persoaneRepo;
    private final NevoiRepo nevoiRepo;
    private final List<Observer> observers = new ArrayList<>();
    private Persoana currentUser = null;

    public Service(PersoaneRepo persoaneRepo, NevoiRepo nevoiRepo) {
        this.persoaneRepo = persoaneRepo;
        this.nevoiRepo = nevoiRepo;
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(Observer::update);
    }

    public void register(String nume, String prenume, String username, String parola,
                         Oras oras, String strada, String numarStrada, String telefon) throws IOException {
        Persoana persoana = new Persoana(nume, prenume, username, parola, oras, strada, numarStrada, telefon);
        Optional<Persoana> saved = persoaneRepo.save(persoana);
        if (saved.isEmpty()) {
            throw new IOException("Nu s-a putut salva persoana!");
        }
        currentUser = saved.get();
    }

    public Persoana login(String username) throws IOException {
        for (Persoana persoana : persoaneRepo.findAll()) {
            if (persoana.getUsername().equals(username)) {
                currentUser = persoana;
                return persoana;
            }
        }
        throw new IOException("User negasit!");
    }

    public List<Nevoie> getNevoiPentruOras(Oras oras) throws IOException {
        if (currentUser == null) {
            throw new IllegalStateException("Nu exista user logat!");
        }
        return StreamSupport.stream(nevoiRepo.findAll().spliterator(), false)
                .filter(nevoie -> {
                    try {
                        Optional<Persoana> omInNevoie = persoaneRepo.findOne(nevoie.getOmInNevoie());
                        return omInNevoie.isPresent() &&
                                omInNevoie.get().getOras() == oras &&
                                !nevoie.getOmInNevoie().equals(currentUser.getId()) &&
                                nevoie.getStatus() == Status.Caut_Erou;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public void acceptaNevoie(Long nevoieId) throws IOException {
        if (currentUser == null) {
            throw new IllegalStateException("Nu exista user logat!");
        }

        Optional<Nevoie> nevoieOpt = nevoiRepo.findOne(nevoieId);
        if (nevoieOpt.isEmpty()) {
            throw new IOException("Nevoia nu exista!");
        }

        Nevoie nevoie = nevoieOpt.get();
        if (nevoie.getStatus() != Status.Caut_Erou) {
            throw new IOException("Nevoia are deja un erou!");
        }

        nevoie.setOmSalvator(currentUser.getId());
        nevoie.setStatus(Status.Erou_Gasit);
        nevoiRepo.update(nevoie);
        notifyObservers();
    }

    public List<Nevoie> getFapteBuneForCurrentUser() throws IOException {
        if (currentUser == null) {
            throw new IllegalStateException("Nu exista user logat!");
        }

        return StreamSupport.stream(nevoiRepo.findAll().spliterator(), false)
                .filter(nevoie -> currentUser.getId().equals(nevoie.getOmSalvator()))
                .collect(Collectors.toList());
    }

    public void adaugaNevoie(String titlu, String descriere, LocalDateTime deadline) throws IOException {
        if (currentUser == null) {
            throw new IllegalStateException("Nu exista user logat!");
        }

        // Create new Nevoie with null omSalvator
        Nevoie nevoie = new Nevoie(titlu, descriere, deadline, currentUser.getId(), null, Status.Caut_Erou);
        Optional<Nevoie> savedNevoie = nevoiRepo.save(nevoie);

        if (savedNevoie.isEmpty()) {
            throw new IOException("Nu s-a putut salva nevoia!");
        }

        // Notify observers after successful save
        notifyObservers();
    }

    public List<String> getAllUsernames() throws IOException {
        return StreamSupport.stream(persoaneRepo.findAll().spliterator(), false)
                .map(Persoana::getUsername)
                .collect(Collectors.toList());
    }

    public Persoana getCurrentUser() {
        return currentUser;
    }
}