package com.autoCenterSilva.demo.cotacao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CotacaoMoedaDTO {
    @JsonProperty("USDBRL")
    private DadosMoeda usdBrl;

    public DadosMoeda getUsdBrl() {
        return usdBrl;
    }

    public void setUsdBrl(DadosMoeda usdBrl) {
        this.usdBrl = usdBrl;
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
