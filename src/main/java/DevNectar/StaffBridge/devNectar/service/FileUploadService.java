package DevNectar.StaffBridge.devNectar.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    // Define the base folder where all files will go (Inside the 'resources' folder)
    private final String BASE_UPLOAD_FOLDER = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "Data-files";

    // Define sub-folders for profiles and resumes within Data-files
    private final String PROFILES_FOLDER = BASE_UPLOAD_FOLDER + File.separator + "profile-images";
    private final String RESUMES_FOLDER = BASE_UPLOAD_FOLDER + File.separator + "resumes";

    // Constructor to create folders when the application starts
    public FileUploadService() {
        createFolderIfNotExists(BASE_UPLOAD_FOLDER);
        createFolderIfNotExists(PROFILES_FOLDER);
        createFolderIfNotExists(RESUMES_FOLDER);
    }

    // A simple function to create a folder
    private void createFolderIfNotExists(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // Creates the folder and any missing parent folders
        }
    }

    // Function to save a Profile Image and return the short path
    public String saveProfileImage(MultipartFile file) {
        if (file.isEmpty()) {
            return null; // Don't do anything if file is empty
        }
        
        try {
            // Get original file name
            String originalFileName = file.getOriginalFilename();
            
            // Create a unique file name so we don't overwrite other files
            // E.g., unique-id-image.jpg
            String uniqueFileName = UUID.randomUUID().toString() + "-" + originalFileName;
            
            // Create the full path to exactly where the file will be saved
            Path exactFilePath = Paths.get(PROFILES_FOLDER, uniqueFileName);
            
            // Actually save (copy) the file to the drive folder
            Files.copy(file.getInputStream(), exactFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the "short path" to save into our database
            // It will look like: /Data-files/profile-images/unique-id-image.jpg
            return "/Data-files/profile-images/" + uniqueFileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null if there is an error
        }
    }

    // Function to save a Resume and return the short path
    public String saveResumeFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null; // Don't do anything if file is empty
        }
        
        try {
            // Get original file name
            String originalFileName = file.getOriginalFilename();
            
            // Create a unique file name
            String uniqueFileName = UUID.randomUUID().toString() + "-" + originalFileName;
            
            // Create the full path to exactly where the file will be saved
            Path exactFilePath = Paths.get(RESUMES_FOLDER, uniqueFileName);
            
            // Actually save (copy) the file to the drive folder
            Files.copy(file.getInputStream(), exactFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the "short path" to save into our database
            // It will look like: /Data-files/resumes/unique-id-resume.pdf
            return "/Data-files/resumes/" + uniqueFileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null if there is an error
        }
    }
}
