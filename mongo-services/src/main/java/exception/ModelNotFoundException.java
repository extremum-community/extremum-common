package exception;

import com.extremum.common.exception.CommonException;
import org.springframework.http.HttpStatus;

public class ModelNotFoundException extends CommonException {
    private Class<?> modelClass;
    private String modelId;

    public ModelNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }

    public ModelNotFoundException(Class<?> modelClass, String modelId) {
        this("Model " + modelClass.getSimpleName() + " with ID " + modelId + " was not found");
        this.modelClass = modelClass;
        this.modelId = modelId;
    }

    public Class<?> getModelClass() {
        return modelClass;
    }

    public String getModelId() {
        return modelId;
    }
}
