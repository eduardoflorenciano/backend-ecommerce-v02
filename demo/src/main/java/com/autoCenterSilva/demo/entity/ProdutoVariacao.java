package com.autoCenterSilva.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "produtos_variacao")
public class ProdutoVariacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medida_completa")
    private String medida;

    @Column(name = "largura")
    private Integer largura;

    @Column(name = "perfil")
    private Integer perfil;

    @Column(name = "aro")
    private Integer aro;

    @Column(name = "indice_carga")
    private Integer indiceCarga;

    @Column(name = "preco")
    private BigDecimal preco;

    @Column(name = "quantidade_estoque")
    private Integer quantidadeEstoque;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produtos produto;

    @ManyToMany(mappedBy = "variacoes")
    private Set<Carros> carros = new HashSet<>();

}
