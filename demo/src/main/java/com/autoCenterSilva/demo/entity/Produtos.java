package com.autoCenterSilva.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "produtos")
public class Produtos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "modelo")
    String modelo;

    @Column(name = "marca")
    private String marca;

    @Column(name = "ativo")
    private Boolean ativo;

    @OneToMany(mappedBy = "produto")
    private List<ProdutoVariacao> variacao = new ArrayList<>();
}
