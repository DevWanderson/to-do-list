package io.github.devwanderson.todolist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController // Define o controlador para a execução do projeto
@RequestMapping
public class Mycontroller {
    //Definir Métodos


    //Método (Funcionalidade) de uma classe
    @GetMapping("/douglas")
    public String firstMessage(){
        return "Agora Foi";
    }
}
