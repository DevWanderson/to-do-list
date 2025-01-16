package io.github.devwanderson.todolist.task;

import io.github.devwanderson.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity<?> create (@RequestBody TaskModel taskModel, Principal principal){
        UUID idUser = getKeycloakUserId(principal);
        taskModel.setIdUser(idUser);

        //DATETIME VERIFICATION
        var currentDate = LocalDateTime.now();
        //(REGRA-1: UMA TAREFA NÃO PODE TER A DATA DE INÍCIO E TÉRMINO MENOR DO QUE A DATA ATUAL)
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/fim deve ser maior que a data atual");
        }
        //(REGRA - 2: UMA TAREFA NÃO PODE TER O INÍCIO POSTERIOR A DATA DE TÉRMINO)
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve anterior a data de término");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    private UUID getKeycloakUserId(Principal principal){
        if (principal instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken keycloakToken = (KeycloakAuthenticationToken) principal;
            KeycloakPrincipal<?> keycloakPrincipal = (KeycloakPrincipal<?>) keycloakToken.getPrincipal();
            String id = keycloakPrincipal.getKeycloakSecurityContext().getToken().getSubject();
            return UUID.fromString(id);
        }
        return null;
    }

    @GetMapping("/")
    public List<TaskModel> list(Principal principal) {
        UUID idUser = getKeycloakUserId(principal);
        return this.taskRepository.findByIdUser(idUser);
    }

    //Verificar sempre se o servletPath não está 'travado' apenas para o contexto definido
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody TaskModel taskModel, Principal principal, @PathVariable UUID id){
        var task = this.taskRepository.findById(id).orElse(null);
        UUID idUser = getKeycloakUserId(principal);

        if (task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada!");
        }

        if (!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário sem permissão para alterar a tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.ok().body(taskUpdated);
    }
}
