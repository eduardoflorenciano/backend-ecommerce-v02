package com.autoCenterSilva.demo.cotacao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CotacaoMoedaDTO {
    @JsonProperty("USDBRL")
    private DadosMoeda USDBRL;

    public DadosMoeda getUSDBRL() {
        return USDBRL;
    }

    public void setUSDBRL(DadosMoeda USDBRL) {
        this.USDBRL = USDBRL;
    }

    public static class DadosMoeda {
        @JsonProperty("bid")
        private String bid;

        public String getBid() {
            return bid;
        }

        public void setBid(String bid) {
            this.bid = bid;
        }
    }
}
