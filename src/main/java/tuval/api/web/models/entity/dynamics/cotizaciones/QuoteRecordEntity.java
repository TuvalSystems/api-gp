package tuval.api.web.models.entity.dynamics.cotizaciones;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Table(name = "tbm_cabcotrec", schema = "dbo" , catalog = "Dynamics2021")
public class QuoteRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "co_tbcot")
    private Integer coTbcot;

    @Column(name = "tx_cot")
    private String txCot;

    @Column(name = "tx_usr")
    private String txUsr;

    @Column(name = "tx_cliRuc")
    private String txCliRuc;

    @Column(name = "tx_cliRazSoc")
    private String txCliRazSoc;

    @Column(name = "tx_cotCliSri")
    private String txCotCliSri;

    @Column(name = "tx_est")
    private String txEst;

    @Column(name = "tx_bod")
    private String txBod;

    @Column(name = "tx_ptoVta")
    private String txPtoVta;

    @Column(name = "tx_ptoVtaDesc")
    private String txPtoVtaDesc;

    @Column(name = "tx_cotReqapr")
    private String txCotReqapr;

    @Column(name = "tx_cotAprCom")
    private String txCotAprCom;

    @Column(name = "tx_cotMot")
    private String txCotMot;

    @Column(name = "co_cotRec")
    private Integer coCotRec;

    @Column(name = "fe_cotRec")
    private LocalDate feCotRec;

    @Column(name = "tx_obsMot")
    private String txObsMot;

    @Column(name = "fe_ing")
    private LocalDate feIng;

    @Column(name = "tx_usrIng")
    private String txUsrIng;

    @Column(name = "fe_ultmod")
    private LocalDate feUltmod;

    @Column(name = "tx_usrMod")
    private String txUsrMod;

    // Getters y Setters
    public Integer getCoTbcot() {
        return coTbcot;
    }

    public void setCoTbcot(Integer coTbcot) {
        this.coTbcot = coTbcot;
    }

    public String getTxCot() {
        return txCot;
    }

    public void setTxCot(String txCot) {
        this.txCot = txCot;
    }

    public String getTxUsr() {
        return txUsr;
    }

    public void setTxUsr(String txUsr) {
        this.txUsr = txUsr;
    }

    public String getTxCliRuc() {
        return txCliRuc;
    }

    public void setTxCliRuc(String txCliRuc) {
        this.txCliRuc = txCliRuc;
    }

    public String getTxCliRazSoc() {
        return txCliRazSoc;
    }

    public void setTxCliRazSoc(String txCliRazSoc) {
        this.txCliRazSoc = txCliRazSoc;
    }

    public String getTxCotCliSri() {
        return txCotCliSri;
    }

    public void setTxCotCliSri(String txCotCliSri) {
        this.txCotCliSri = txCotCliSri;
    }

    public String getTxEst() {
        return txEst;
    }

    public void setTxEst(String txEst) {
        this.txEst = txEst;
    }

    public String getTxBod() {
        return txBod;
    }

    public void setTxBod(String txBod) {
        this.txBod = txBod;
    }

    public String getTxPtoVta() {
        return txPtoVta;
    }

    public void setTxPtoVta(String txPtoVta) {
        this.txPtoVta = txPtoVta;
    }

    public String getTxPtoVtaDesc() {
        return txPtoVtaDesc;
    }

    public void setTxPtoVtaDesc(String txPtoVtaDesc) {
        this.txPtoVtaDesc = txPtoVtaDesc;
    }

    public String getTxCotReqapr() {
        return txCotReqapr;
    }

    public void setTxCotReqapr(String txCotReqapr) {
        this.txCotReqapr = txCotReqapr;
    }

    public String getTxCotAprCom() {
        return txCotAprCom;
    }

    public void setTxCotAprCom(String txCotAprCom) {
        this.txCotAprCom = txCotAprCom;
    }

    public String getTxCotMot() {
        return txCotMot;
    }

    public void setTxCotMot(String txCotMot) {
        this.txCotMot = txCotMot;
    }

    public Integer getCoCotRec() {
        return coCotRec;
    }

    public void setCoCotRec(Integer coCotRec) {
        this.coCotRec = coCotRec;
    }

    public LocalDate getFeCotRec() {
        return feCotRec;
    }

    public void setFeCotRec(LocalDate feCotRec) {
        this.feCotRec = feCotRec;
    }

    public String getTxObsMot() {
        return txObsMot;
    }

    public void setTxObsMot(String txObsMot) {
        this.txObsMot = txObsMot;
    }

    public LocalDate getFeIng() {
        return feIng;
    }

    public void setFeIng(LocalDate feIng) {
        this.feIng = feIng;
    }

    public String getTxUsrIng() {
        return txUsrIng;
    }

    public void setTxUsrIng(String txUsrIng) {
        this.txUsrIng = txUsrIng;
    }

    public LocalDate getFeUltmod() {
        return feUltmod;
    }

    public void setFeUltmod(LocalDate feUltmod) {
        this.feUltmod = feUltmod;
    }

    public String getTxUsrMod() {
        return txUsrMod;
    }

    public void setTxUsrMod(String txUsrMod) {
        this.txUsrMod = txUsrMod;
    }
}
