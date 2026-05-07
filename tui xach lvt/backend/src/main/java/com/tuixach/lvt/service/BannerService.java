package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.BannerDTO;
import com.tuixach.lvt.entity.Banner;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final FileStorageService fileStorageService;

    public List<BannerDTO> getActiveBanners() {
        return bannerRepository.findByActiveTrueOrderByDisplayOrder().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BannerDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BannerDTO createBanner(BannerDTO request, MultipartFile image, MultipartFile subImage) throws IOException {
        String imageUrl = request.getImageUrl();
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.storeFile(image);
        }

        String subImageUrl = request.getSubImageUrl();
        if (subImage != null && !subImage.isEmpty()) {
            subImageUrl = fileStorageService.storeFile(subImage);
        }

        Banner banner = Banner.builder()
                .title(request.getTitle())
                .imageUrl(imageUrl)
                .subImageUrl(subImageUrl)
                .linkUrl(request.getLinkUrl())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();
        bannerRepository.save(banner);
        return mapToDTO(banner);
    }

    public BannerDTO updateBanner(Long id, BannerDTO request, MultipartFile image, MultipartFile subImage) throws IOException {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner"));

        banner.setTitle(request.getTitle());

        if (image != null && !image.isEmpty()) {
            if (banner.getImageUrl() != null && banner.getImageUrl().startsWith("/uploads/")) {
                fileStorageService.deleteFile(banner.getImageUrl());
            }
            banner.setImageUrl(fileStorageService.storeFile(image));
        } else if (request.getImageUrl() != null) {
            banner.setImageUrl(request.getImageUrl());
        }

        if (subImage != null && !subImage.isEmpty()) {
            if (banner.getSubImageUrl() != null && banner.getSubImageUrl().startsWith("/uploads/")) {
                fileStorageService.deleteFile(banner.getSubImageUrl());
            }
            banner.setSubImageUrl(fileStorageService.storeFile(subImage));
        } else if (request.getSubImageUrl() != null) {
            banner.setSubImageUrl(request.getSubImageUrl());
        }

        banner.setLinkUrl(request.getLinkUrl());
        banner.setDisplayOrder(request.getDisplayOrder());
        banner.setActive(request.isActive());

        bannerRepository.save(banner);
        return mapToDTO(banner);
    }

    public void deleteBanner(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy banner");
        }
        bannerRepository.deleteById(id);
    }

    private BannerDTO mapToDTO(Banner banner) {
        return BannerDTO.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .subImageUrl(banner.getSubImageUrl())
                .linkUrl(banner.getLinkUrl())
                .displayOrder(banner.getDisplayOrder())
                .active(banner.isActive())
                .build();
    }
}
