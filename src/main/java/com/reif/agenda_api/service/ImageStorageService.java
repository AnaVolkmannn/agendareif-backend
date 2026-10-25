package com.reif.agenda_api.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Set<String> TIPOS_ACEITOS = Set.of("image/png", "image/jpeg");
    private static final long TAMANHO_MAX_BYTES = 10L * 1024 * 1024;

    private final BlobContainerClient container;

    public ImageStorageService(BlobContainerClient container) {
        this.container = container;
    }

    public String upload(MultipartFile file, String pasta) {
        validar(file);

        String extensao = "image/png".equals(file.getContentType()) ? "png" : "jpg";
        String blobName = pasta + "/" + UUID.randomUUID() + "." + extensao;
        BlobClient blob = container.getBlobClient(blobName);

        try (InputStream in = file.getInputStream()) {
            blob.upload(in, file.getSize(), true);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível enviar a imagem.", e);
        }

        blob.setHttpHeaders(new BlobHttpHeaders()
                .setContentType(file.getContentType())
                .setCacheControl("public, max-age=31536000"));

        return blob.getBlobUrl();
    }

    /** Chame ao excluir foto do portfólio, serviço etc. para não deixar arquivo órfão. */
    public void deleteByUrl(String url) {
        if (url == null) return;
        String prefixo = container.getBlobContainerUrl() + "/";
        if (!url.startsWith(prefixo)) return; // URL de outro lugar: ignora
        container.getBlobClient(url.substring(prefixo.length())).deleteIfExists();
    }

    private void validar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo de imagem.");
        }
        if (!TIPOS_ACEITOS.contains(file.getContentType())) {
            throw new IllegalArgumentException("Formato não suportado. Envie uma imagem PNG ou JPG.");
        }
        if (file.getSize() > TAMANHO_MAX_BYTES) {
            throw new IllegalArgumentException("A imagem excede o limite de 10 MB.");
        }
        // Não confia só no Content-Type: confere se o conteúdo é mesmo uma imagem.
        try (InputStream in = file.getInputStream()) {
            if (ImageIO.read(in) == null) {
                throw new IllegalArgumentException("O arquivo enviado não é uma imagem válida.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("O arquivo enviado não é uma imagem válida.");
        }
    }
}