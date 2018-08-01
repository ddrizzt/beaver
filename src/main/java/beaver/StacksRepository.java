package beaver;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface StacksRepository extends  CrudRepository<Stacks, Integer> {

    @RestResource(path="*", rel="*")
    List<Stacks> findByStackname(@Param("stackname") String stackname);
}
