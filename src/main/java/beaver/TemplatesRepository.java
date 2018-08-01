package beaver;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface TemplatesRepository extends CrudRepository<Templates, Integer> {

    @RestResource(path="*", rel="*")
    List<Templates> findByTemplatename(@Param("templatename") String templatename);
}
