package np1815.feedback.metricsbackend.api;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Provider
public class LocalDateTimeProvider implements ParamConverterProvider {

    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.getName().equals(LocalDateTime.class.getName())) {
            return new ParamConverter<T>() {
                @SuppressWarnings("unchecked")
                public T fromString(String value) {
                    return value!=null ? (T) LocalDateTime.parse(value) : null;
                }

                public String toString(T bean) {
                    return bean!=null ? bean.toString() : "";
                }
            };
        }
        return null;
    }
}