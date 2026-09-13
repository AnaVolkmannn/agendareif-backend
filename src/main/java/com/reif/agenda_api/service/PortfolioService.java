package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Portfolio;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ProfessionalService professionalService;

    public PortfolioService(PortfolioRepository portfolioRepository,
                             ProfessionalService professionalService) {
        this.portfolioRepository = portfolioRepository;
        this.professionalService = professionalService;
    }

    @Transactional
    public Portfolio create(Long professionalId, String imageUrl) {
        Professional professional = professionalService.findById(professionalId);

        Portfolio portfolio = new Portfolio();
        portfolio.setProfessional(professional);
        portfolio.setImageUrl(imageUrl);
        portfolio.setSortOrder(nextSortOrder(professionalId));

        return portfolioRepository.save(portfolio);
    }

    @Transactional
    public List<Portfolio> createBatch(Long professionalId, List<String> imageUrls) {
        Professional professional = professionalService.findById(professionalId);

        int sortOrder = nextSortOrder(professionalId);
        List<Portfolio> photos = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            Portfolio portfolio = new Portfolio();
            portfolio.setProfessional(professional);
            portfolio.setImageUrl(imageUrl);
            portfolio.setSortOrder(sortOrder++);
            photos.add(portfolio);
        }

        return portfolioRepository.saveAll(photos);
    }

    public List<Portfolio> findAllByProfessional(Long professionalId) {
        professionalService.findById(professionalId);
        return portfolioRepository.findByProfessionalIdOrderBySortOrderAsc(professionalId);
    }

    @Transactional
    public void delete(Long professionalId, Long id) {
        Portfolio portfolio = findEntity(professionalId, id);
        int removedSortOrder = portfolio.getSortOrder();

        portfolioRepository.delete(portfolio);
        portfolioRepository.flush();

        closeGap(professionalId, removedSortOrder);
    }

    @Transactional
    public List<Portfolio> reorder(Long professionalId, List<Long> orderedIds) {
        List<Portfolio> current = portfolioRepository.findByProfessionalIdOrderBySortOrderAsc(professionalId);

        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (orderedIds.size() != current.size() || uniqueIds.size() != orderedIds.size()) {
            throw new IllegalArgumentException(
                    "A lista enviada deve conter, sem repetição, exatamente as " +
                            current.size() + " fotos já cadastradas para esse profissional.");
        }

        Map<Long, Portfolio> byId = new HashMap<>();
        for (Portfolio portfolio : current) {
            byId.put(portfolio.getId(), portfolio);
        }

        List<Portfolio> reordered = new ArrayList<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            Long photoId = orderedIds.get(i);
            Portfolio portfolio = byId.get(photoId);
            if (portfolio == null) {
                throw new IllegalArgumentException(
                        "A foto de id " + photoId + " não pertence a esse profissional.");
            }
            portfolio.setSortOrder(i);
            reordered.add(portfolio);
        }

        return portfolioRepository.saveAll(reordered);
    }

    private Portfolio findEntity(Long professionalId, Long id) {
        return portfolioRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Foto do portfólio não encontrada."));
    }

    private int nextSortOrder(Long professionalId) {
        return portfolioRepository.findMaxSortOrderByProfessionalId(professionalId) + 1;
    }

    private void closeGap(Long professionalId, int removedSortOrder) {
        List<Portfolio> remaining = portfolioRepository.findByProfessionalIdOrderBySortOrderAsc(professionalId);
        List<Portfolio> toUpdate = new ArrayList<>();

        for (Portfolio portfolio : remaining) {
            if (portfolio.getSortOrder() > removedSortOrder) {
                portfolio.setSortOrder(portfolio.getSortOrder() - 1);
                toUpdate.add(portfolio);
            }
        }

        if (!toUpdate.isEmpty()) {
            portfolioRepository.saveAll(toUpdate);
        }
    }
}