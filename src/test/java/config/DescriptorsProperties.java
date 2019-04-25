package config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("descriptors")
public class DescriptorsProperties {
    private String descriptorsMapName;
    private String internalIdsMapName;
    private String collectionDescriptorsMapName;
    private String collectionCoordinatesMapName;
}
