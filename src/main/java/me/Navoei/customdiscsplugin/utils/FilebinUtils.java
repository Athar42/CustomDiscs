package me.Navoei.customdiscsplugin.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

public class FilebinUtils {

    public static final String CUSTOMDISCS_USER_AGENT = "CustomDiscs/curl";
    private static final String FILEBIN_HOST = "filebin.net";

    private static long getFilebinPathSegmentCount(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null || (!host.equalsIgnoreCase(FILEBIN_HOST) && !host.equalsIgnoreCase("www." + FILEBIN_HOST))) return -1;
            String path = uri.getPath();
            if (path == null) return -1;
            return java.util.Arrays.stream(path.split("/")).filter(pathSegment -> !pathSegment.isEmpty()).count();
        } catch (URISyntaxException e) {
            return -1;
        }
    }

    public static boolean isFilebinUrl(String url) {
        long pathSegmentCount = getFilebinPathSegmentCount(url);
        return pathSegmentCount == 1 || pathSegmentCount == 2;
    }

    public static boolean isFilebinDirectUrl(String url) {
        return getFilebinPathSegmentCount(url) == 2;
    }

    public record FilebinFileInfo(String filename, long bytes, URL downloadUrl) {}

    public static FilebinFileInfo getFirstAudioFile(String filebinUrl, int maxSizeInMb) throws IOException, FileTooLargeException {
        URI filebinUri;
        try {
            filebinUri = new URI(filebinUrl);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid Filebin URL: " + filebinUrl, e);
        }

        HttpURLConnection connection = (HttpURLConnection) filebinUri.toURL().openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", CUSTOMDISCS_USER_AGENT);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.connect();

        int statusCode = connection.getResponseCode();
        if (statusCode != 200) {
            throw new IOException("Filebin API responded with status " + statusCode + " for " + filebinUrl);
        }

        JsonElement jsonOutput;
        try (InputStreamReader streamReader = new InputStreamReader(connection.getInputStream())) {
            jsonOutput = JsonParser.parseReader(streamReader);
        }

        if (!(jsonOutput instanceof JsonObject jsonObject)) {
            throw new IOException("Unexpected Filebin API response format");
        }

        JsonElement filesElement = jsonObject.get("files");
        if (!(filesElement instanceof JsonArray files) || files.isEmpty()) {
            throw new NoAudioFilesException("No files found in Filebin bin: " + filebinUrl);
        }

        for (JsonElement jsonElement : files) {
            if (!(jsonElement instanceof JsonObject file)) continue;

            JsonElement contentTypeElement = file.get("content-type");
            if (contentTypeElement == null) continue;
            String contentTypeElementAsString = contentTypeElement.getAsString();

            if (!isSupportedContentType(contentTypeElementAsString)) continue;

            long bytes = file.get("bytes").getAsLong();
            long sizeInMb = bytes / 1048576;
            if (sizeInMb > maxSizeInMb) {
                throw new FileTooLargeException(sizeInMb, maxSizeInMb);
            }

            String filenameString = file.get("filename").getAsString();
            URI fileUri;
            try {
                fileUri = new URI(filebinUrl + "/" + new URI(null, null, filenameString, null).toASCIIString());
            } catch (URISyntaxException e) {
                throw new IOException("Failed to build Filebin file URL for: " + filenameString, e);
            }

            return new FilebinFileInfo(filenameString, bytes, fileUri.toURL());
        }

        throw new NoAudioFilesException("No supported audio file (wav/mp3/flac) found in Filebin bin: " + filebinUrl);
    }

    public static void downloadFilebinFile(URL filebinUrl, File destination, int maxSizeInMb) throws IOException, FileTooLargeException {
        HttpURLConnection connection = (HttpURLConnection) filebinUrl.openConnection();
        connection.setRequestProperty("User-Agent", CUSTOMDISCS_USER_AGENT);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000);
        connection.connect();

        int statusCode = connection.getResponseCode();
        if (statusCode != 200) {
            throw new IOException("Filebin download responded with status " + statusCode);
        }

        long contentLength = connection.getContentLengthLong();
        if (contentLength > 0) {
            long sizeInMb = contentLength / 1048576;
            if (sizeInMb > maxSizeInMb) {
                throw new FileTooLargeException(sizeInMb, maxSizeInMb);
            }
        }

        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, destination.toPath());
        }
    }

    private static boolean isSupportedContentType(String contentType) {
        return contentType.equals("audio/wav") || contentType.equals("audio/x-wav") || contentType.equals("audio/mpeg") || contentType.equals("audio/flac") || contentType.equals("audio/x-flac");
    }

    public static class FileTooLargeException extends Exception {
        public final long actualSizeInMb;
        public final long maxSizeInMb;

        public FileTooLargeException(long actualSizeInMb, long maxSizeInMb) {
            super("File size " + actualSizeInMb + "MB exceeds maximum of " + maxSizeInMb + "MB");
            this.actualSizeInMb = actualSizeInMb;
            this.maxSizeInMb = maxSizeInMb;
        }
    }

    public static class NoAudioFilesException extends IOException {
        public NoAudioFilesException(String message) {
            super(message);
        }
    }
}
