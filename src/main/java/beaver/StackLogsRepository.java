package beaver;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface StackLogsRepository extends  CrudRepository<StackLogs, Integer> {

    @RestResource(path="*", rel="*")
    List<StackLogs> findByUsername(@Param("username") String username);

    @RestResource(path="*", rel="*")
    List<StackLogs> findByStackid(@Param("stackid") Integer stackid);

    @RestResource(path="*", rel="*")
    List<StackLogs> findByStackidAndStatus(@Param("stackid") Integer stackid, @Param("status") Integer status);
}
