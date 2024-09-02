package io.github.devwanderson.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.github.devwanderson.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
    //utilizar repositório de usuários
    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var servletPath = (request.getServletPath());
        if (servletPath.startsWith("/tasks/")) {

            //Pegar a autenticação
            var authorization = request.getHeader("Authorization");
            var authEncoded = authorization.substring("Basic".length()).trim();
            //System.out.println(authEncoded);

            //Desencryptar
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);

            var authString = new String(authDecode);
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

//            System.out.println(username);
//            System.out.println(password);

            //Validar Usuário
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401);
                System.out.println("Usuário Incorreto ou Inexistente!");
            } else {
                //Validar Senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passwordVerify.verified){
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                    System.out.println("Senha Incorreta");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
