package anonymized.shadedetector;

import com.google.common.base.Preconditions;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;

import java.util.ServiceLoader;

/**
 * Instantiates a class via a service loader, and configures it.
 * Services are detected by a unique name returned by name().
 * Service definitions have the following format:  name (? (key=value)(&key=value)*)?
 * @author jens dietrich
 */
public abstract class AbstractServiceLoaderFactory<T extends NamedService> {

    protected abstract Logger getLogger();
    protected Logger LOGGER = getLogger();

    public abstract T create(String configuration);

    public abstract T getDefault();

    protected T create(Class<T> serviceType, String serviceDescription, String configuration) {
        String[] parts = configuration.split("\\?");
        String name = parts[0];
        ServiceLoader<T> loader = ServiceLoader.load(serviceType);
        T service = loader.stream()
            .map(pm-> pm.get())
            .filter(s -> name.equals(s.name()))
            .findFirst().orElse(null);

        if (service==null) {
            Preconditions.checkArgument(false,"no " + serviceDescription + " found with name " + name);
        }

        LOGGER.info("Instantiated {} {}",serviceDescription,name);

        if (parts.length>1)  {
            LOGGER.info("Configuring {}: {}",serviceDescription,name);
            Preconditions.checkArgument(parts.length==2);
            String configString = parts[1];
            String[] configParts = configString.split("&");
            for (String configDef:configParts) {
                String[] keyValue = configDef.split("=");
                Preconditions.checkArgument(keyValue.length==2,"syntax error in property definition " + configDef);
                String key = keyValue[0];
                String value = keyValue[1];
                LOGGER.info("\tset property {} -> {}",key,value);
                try {
                    BeanUtils.setProperty(service,key,value);
                } catch (Exception e) {
                    LOGGER.warn("Cannot set property {} to value {} for service {} {} of type {}",key,value,serviceDescription,service.name(),service.getClass().getName(),e);
                }
            }
        }

        return service;
    }
}
