package DevNectar.StaffBridge.devNectar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RegistrationApprovalInterceptor registrationApprovalInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(registrationApprovalInterceptor);
    }

    // Connect specific web links (URLs) to folders on our hard drive
    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        // Find the absolute path to our "Data-files" folder dynamically inside resources
        java.nio.file.Path uploadFolder = java.nio.file.Paths.get("src", "main", "resources", "Data-files");
        String fullUploadPath = uploadFolder.toFile().getAbsolutePath();

        // Any link starting with /Data-files/ goes looking in the "Data-files" folder
        registry.addResourceHandler("/Data-files/**")
                .addResourceLocations("file:" + fullUploadPath + "/");
    }
}
