package pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onpe.sceorcbackend.model.dto.TokenInfo;
import pe.gob.onpe.sceorcbackend.model.dto.response.DigitizationGetFilesResponse;
import pe.gob.onpe.sceorcbackend.model.dto.response.GenericResponse;
import pe.gob.onpe.sceorcbackend.model.dto.response.resoluciones.ActaBean;
import pe.gob.onpe.sceorcbackend.model.dto.response.resoluciones.ActaOficioBean;
import pe.gob.onpe.sceorcbackend.model.dto.response.resoluciones.DetActaOficioBean;
import pe.gob.onpe.sceorcbackend.model.dto.response.resoluciones.SeguimientoOficioDTO;
import pe.gob.onpe.sceorcbackend.model.enums.TransmisionNacionEnum;
import pe.gob.onpe.sceorcbackend.model.postgresql.admin.entity.DetTipoEleccionDocumentoElectoral;
import pe.gob.onpe.sceorcbackend.model.postgresql.admin.services.DetTipoEleccionDocumentoElectoralService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Acta;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ActaCeleste;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Archivo;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.CentroComputo;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaFormato;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.DetActaOficio;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.JuradoElectoralEspecial;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.Oficio;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.ProcesoElectoral;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.entity.TabResolucion;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.ActaCelesteRepository;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.ActaRepository;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.ArchivoRepository;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.DetActaFormatoRepository;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.DetActaOficioRepository;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.JuradoElectoralEspecialRepository;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.repository.OficioRepository;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.ActaTransmisionNacionStrategyService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.CentroComputoService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.MaeProcesoElectoralService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.OficioService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.StorageService;
import pe.gob.onpe.sceorcbackend.model.postgresql.bd.service.UtilSceService;
import pe.gob.onpe.sceorcbackend.utils.ConstantesCatalogo;
import pe.gob.onpe.sceorcbackend.utils.ConstantesComunes;
import pe.gob.onpe.sceorcbackend.utils.ConstantesEstadoActa;
import pe.gob.onpe.sceorcbackend.utils.ConstantesOficio;
import pe.gob.onpe.sceorcbackend.utils.OficioUtils;
import pe.gob.onpe.sceorcbackend.utils.PathUtils;
import pe.gob.onpe.sceorcbackend.utils.funciones.Funciones;

@Service
public class OficioServiceImpl implements OficioService {

    Logger logger = LoggerFactory.getLogger(OficioServiceImpl.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final OficioRepository oficioRepository;
    private final DetActaOficioRepository detActaOficioRepository;
    private final ActaRepository actaRepository;
    private final ActaCelesteRepository actaCelesteRepository;
    private final MaeProcesoElectoralService procesoElectoralService;
    private final UtilSceService utilSceService;
    private final DetActaFormatoRepository detActaFormatoRepository;
    private final StorageService storageService;
    private final ActaTransmisionNacionStrategyService actaTransmisionNacionStrategyService;
    private final CentroComputoService centroComputoService;
    private final ArchivoRepository archivoRepository;
    private final JuradoElectoralEspecialRepository juradoElectoralEspecialRepository;
    private final DetTipoEleccionDocumentoElectoralService detTipoEleccionDocumentoElectoralService;

    public OficioServiceImpl(OficioRepository oficioRepository, DetActaOficioRepository detActaOficioRepository,
            ActaRepository actaRepository, ActaCelesteRepository actaCelesteRepository,
            MaeProcesoElectoralService procesoElectoralService, UtilSceService utilSceService,
            DetActaFormatoRepository detActaFormatoRepository, StorageService storageService,
            CentroComputoService centroComputoService, ArchivoRepository archivoRepository,
            ActaTransmisionNacionStrategyService actaTransmisionNacionStrategyService,
            JuradoElectoralEspecialRepository juradoElectoralEspecialRepository,
            DetTipoEleccionDocumentoElectoralService detTipoEleccionDocumentoElectoralService) {
        this.oficioRepository = oficioRepository;
        this.detActaOficioRepository = detActaOficioRepository;
        this.actaRepository = actaRepository;
        this.actaCelesteRepository = actaCelesteRepository;
        this.procesoElectoralService = procesoElectoralService;
        this.utilSceService = utilSceService;
        this.detActaFormatoRepository = detActaFormatoRepository;
        this.storageService = storageService;
        this.centroComputoService = centroComputoService;
        this.archivoRepository = archivoRepository;
        this.actaTransmisionNacionStrategyService = actaTransmisionNacionStrategyService;
        this.juradoElectoralEspecialRepository = juradoElectoralEspecialRepository;
        this.detTipoEleccionDocumentoElectoralService = detTipoEleccionDocumentoElectoralService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public GenericResponse<Object> generarOficio(TokenInfo tokenInfo, List<ActaBean> actaBeanList) throws IOException {
        List<DetActaOficioBean> actasValidas = filtrarActasValidas(actaBeanList);
        if (actasValidas.isEmpty()) {
            String mensaje = actaBeanList.size() == 1 ? "La acta no cumple con los requisitos para generar el oficio."
                    : "Ninguna de las actas cumple con los requisitos para generar el oficio.";
            return new GenericResponse<>(false, mensaje);
        }
        Long actaId = actaBeanList.get(0).getActaId();

        Optional<DetActaOficio> detOficioOpt = detActaOficioRepository
                .findFirstByActa_IdOrderByFechaCreacionDesc(actaId);
        if (detOficioOpt.isPresent()) {
            Oficio oficio = detOficioOpt.get().getOficio();
            if (oficio != null && oficio.getArchivo() != null) {
                Long archivoId = oficio.getArchivo().getId();
                Archivo archivo = archivoRepository.findById(archivoId).orElse(null);
                if (archivo != null) {
                    String base64 = OficioUtils
                            .convertToBase64(PathUtils.normalizePath(this.uploadDir, archivo.getGuid()));
                    return new GenericResponse<>(true, archivo.getNombre(), base64);
                } else {
                    return new GenericResponse<>(false, "No se encontro el archivo del oficio.");
                }
            } else {
                return new GenericResponse<>(false, "El oficio no cuenta con archivo registrado.");
            }
        }

        ProcesoElectoral proceso = procesoElectoralService.findByActivo();
        Date fechaActual = new Date();

        Oficio oficio = crearOficio(tokenInfo, fechaActual);
        guardarDetalleActas(actasValidas, oficio, tokenInfo.getNombreUsuario(), fechaActual);

        List<ActaOficioBean> listaReporte = construirListaReporte(actaBeanList, actasValidas);
        byte[] pdf = guardarOficioPdf(oficio, listaReporte, proceso, tokenInfo);

        if (pdf == null || pdf.length == 0) {
            throw new IllegalArgumentException("El archivo no existe");
        }

        return new GenericResponse<>(true, oficio.getNombreOficio(), Base64.getEncoder().encodeToString(pdf));
    }

    private List<DetActaOficioBean> filtrarActasValidas(List<ActaBean> actaBeans) {
        List<DetActaOficioBean> actasValidas = new ArrayList<>();
        for (ActaBean bean : actaBeans) {
            Optional<Acta> actaPloma = actaRepository.findById(bean.getActaId());
            if (actaPloma.isPresent()) {
                Acta acta = actaPloma.get();
                boolean estadoValido = ConstantesEstadoActa.ESTADO_ACTA_PARA_ENVIO_AL_JURADO
                        .equalsIgnoreCase(acta.getEstadoActa());

                Optional<ActaCeleste> actaCelesteOpt = actaCelesteRepository.findByActa_Id(acta.getId());
                boolean celesteValida = actaCelesteOpt.isPresent()
                        && ConstantesEstadoActa.ESTADO_DIGTAL_1ER_CONTROL_ACEPTADA
                                .equalsIgnoreCase(actaCelesteOpt.get().getEstadoDigitalizacion());

                Optional<DetActaFormato> detFormatoOpt = detActaFormatoRepository.findByActa_Id(acta.getId()).stream()
                        .filter(daf -> daf.getCabActaFormato() != null && daf.getCabActaFormato().getFormato() != null
                                && Objects.equals(daf.getCabActaFormato().getFormato().getTipoFormato(),
                                        ConstantesCatalogo.N_CODIGO_CARGO_ENTREGA_ENVIO_JEE)
                                && Objects.equals(daf.getActivo(), ConstantesComunes.ACTIVO))
                        .findFirst();
                boolean cargoGenerado = detFormatoOpt.isPresent();

                if (estadoValido && celesteValida && cargoGenerado) {
                    DetActaOficioBean actaOficioBean = new DetActaOficioBean();
                    actaOficioBean.setActaPlomo(acta);
                    actaOficioBean.setActaCeleste(actaCelesteOpt.get());
                    actaOficioBean.setCabActaFormato(detFormatoOpt.get().getCabActaFormato());
                    actasValidas.add(actaOficioBean);
                }
            }
        }
        return actasValidas;
    }

    private Oficio crearOficio(TokenInfo tokenInfo, Date fechaActual) {
        CentroComputo cc = obtenerCentroComputo(tokenInfo);
        tokenInfo.setCodigoAmbito(cc.getId().toString());
        Oficio oficio = Oficio.builder().nombreOficio(ConstantesComunes.VACIO).centroComputo(cc.getId().intValue())
                .estado(ConstantesOficio.ESTADO_OFICIO_PENDIENTE).activo(ConstantesComunes.ACTIVO)
                .usuarioCreacion(tokenInfo.getNombreUsuario()).fechaCreacion(fechaActual).build();

        oficio = oficioRepository.save(oficio);

        String numeroOficio = OficioUtils.generarNumeroOficio(String.format("%06d", oficio.getId()), tokenInfo);
        oficio.setNombreOficio(numeroOficio);
        return oficioRepository.save(oficio);
    }

    private void guardarDetalleActas(List<DetActaOficioBean> actas, Oficio oficio, String usuario, Date fecha) {
        for (DetActaOficioBean acta : actas) {
            DetActaOficio detalle = new DetActaOficio();
            detalle.setOficio(oficio);
            detalle.setActa(acta.getActaPlomo());
            detalle.setActaCeleste(acta.getActaCeleste());
            detalle.setCabActaFormato(acta.getCabActaFormato());
            detalle.setActivo(ConstantesComunes.ACTIVO);
            detalle.setUsuarioCreacion(usuario);
            detalle.setFechaCreacion(fecha);
            detActaOficioRepository.save(detalle);
        }
    }

    private List<ActaOficioBean> construirListaReporte(List<ActaBean> actaBeanList,
            List<DetActaOficioBean> actasValidas) {
        Map<Long, ActaBean> actaBeanMap = actaBeanList.stream()
                .collect(Collectors.toMap(ActaBean::getActaId, ab -> ab));

        return actasValidas.stream().filter(det -> det.getActaPlomo() != null && det.getActaCeleste() != null)
                .flatMap(det -> {
                    List<ActaOficioBean> reportes = new ArrayList<>();

                    Acta actaPlomo = det.getActaPlomo();
                    ActaCeleste actaCeleste = det.getActaCeleste();

                    ActaBean bean = actaBeanMap.getOrDefault(actaPlomo.getId(), null);
                    String eleccion = bean != null ? bean.getEleccion() : "";

                    ActaOficioBean dtoPlomo = new ActaOficioBean();
                    dtoPlomo.setMesa(actaPlomo.getMesa().getCodigo());
                    dtoPlomo.setCopia(actaPlomo.getNumeroCopia());
                    dtoPlomo.setDigitoChequeo(actaPlomo.getDigitoChequeoEscrutinio());
                    dtoPlomo.setEleccion(eleccion);
                    dtoPlomo.setSobre(ConstantesOficio.TIPO_SOBRE_PLOMO);
                    reportes.add(dtoPlomo);

                    ActaOficioBean dtoCeleste = new ActaOficioBean();
                    dtoCeleste.setMesa(actaPlomo.getMesa().getCodigo());
                    dtoCeleste.setCopia(actaCeleste.getNumeroCopia());
                    dtoCeleste.setDigitoChequeo(actaCeleste.getDigitoChequeoEscrutinio());
                    dtoCeleste.setEleccion(eleccion);
                    dtoCeleste.setSobre(ConstantesOficio.TIPO_SOBRE_CELESTE);
                    reportes.add(dtoCeleste);

                    return reportes.stream();
                }).toList();
    }

    private byte[] guardarOficioPdf(Oficio oficio, List<ActaOficioBean> listaReporte, ProcesoElectoral proceso,
            TokenInfo tokenInfo) {
        byte[] pdf = generarOficioPDF(oficio, listaReporte, proceso, tokenInfo);
        if (pdf == null || pdf.length == 0) {
            throw new IllegalArgumentException("El archivo no existe");
        }

        String nombreArchivo = oficio.getNombreOficio().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
        Archivo archivo = utilSceService.guardarArchivoPdf(pdf, nombreArchivo, tokenInfo);
        if (archivo == null) {
            throw new IllegalArgumentException("No se pudo guardar el archivo del oficio");
        }

        Date ahora = new Date();
        oficio.setArchivo(archivo);
        oficio.setUsuarioModificacion(tokenInfo.getNombreUsuario());
        oficio.setFechaModificacion(ahora);
        oficioRepository.save(oficio);
        return pdf;
    }

    private byte[] generarOficioPDF(Oficio oficio, List<ActaOficioBean> actas, ProcesoElectoral proceso,
            TokenInfo tokenInfo) {
        Map<String, Object> parametros = new HashMap<>();
        ClassLoader loader = getClass().getClassLoader();
        parametros.put(ConstantesComunes.REPORT_PARAM_URL_IMAGE, loader
                .getResourceAsStream(ConstantesComunes.PATH_IMAGE_COMMON + ConstantesComunes.REPORT_PARAM_IMAGEN_ONPE));
        parametros.put(ConstantesComunes.REPORT_PARAM_PIXEL_TRANSPARENTE, loader.getResourceAsStream(
                ConstantesComunes.PATH_IMAGE_COMMON + ConstantesComunes.REPORT_PARAM_IMAGEN_PIXEL_TRANSPARENTE));

        CentroComputo cc = obtenerCentroComputo(tokenInfo);
        JuradoElectoralEspecial jee = juradoElectoralEspecialRepository.findByCodigoCentroComputo(cc.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró Jurado Electoral Especial para código de CC = " + cc.getCodigo()));

        parametros.put(ConstantesComunes.REPORT_PARAM_SIN_VALOR_OFICIAL, utilSceService.getSinValorOficial());
        parametros.put(ConstantesComunes.OFICIO_FECHA, OficioUtils.obtenerFechaOficio(null));
        parametros.put(ConstantesComunes.OFICIO_NUMERO, oficio.getNombreOficio());

        String destinatario = String.join(" ", jee.getApellidoPaternoRepresentante(),
                jee.getApellidoMaternoRepresentante(), jee.getNombresRepresentante()).toUpperCase();
        parametros.put(ConstantesComunes.OFICIO_DESTINATARIO, destinatario);
        parametros.put(ConstantesComunes.OFICIO_CARGO, "JURADO ELECTORAL ESPECIAL " + jee.getNombre());
        parametros.put(ConstantesComunes.OFICIO_DIRECCION, jee.getDireccion().toUpperCase());
        parametros.put(ConstantesComunes.OFICIO_ASUNTO,
                ConstantesOficio.ASUNTO_OFICIO_BODY + " " + proceso.getAcronimo());
        parametros.put(ConstantesComunes.OFICIO_NOMBRE_PROCESO, proceso.getNombre());
        parametros.put(ConstantesComunes.OFICIO_CODIGO_VERIFICACION, "YUXFLJA");

        int cantidadActas = actas.size();
        parametros.put(ConstantesComunes.OFICIO_CANTIDAD_ACTAS, String.valueOf(cantidadActas));
        parametros.put(ConstantesComunes.OFICIO_CANTIDAD_TEXTO, OficioUtils.convertirNumeroATexto(cantidadActas));

        String reportePath = ConstantesComunes.OFICIO_ACTAS_OBSERVADAS_JRXML
                + ConstantesComunes.EXTENSION_REPORTES_JASPER;
        try {
            return Funciones.generarReporte(this.getClass(), actas, reportePath, parametros);
        } catch (Exception e) {
            logger.error("Error al generar el PDF del oficio", e);
            throw new IllegalStateException(
                    "Error al generar el reporte PDF para el oficio ID: " + oficio.getId() + " — " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Oficio oficio) {
        this.oficioRepository.save(oficio);

    }

    @Override
    public void saveAll(List<Oficio> oficios) {
        this.oficioRepository.saveAll(oficios);

    }

    @Override
    public void deleteAll() {
        this.oficioRepository.deleteAll();
    }

    @Override
    public List<Oficio> findAll() {
        return this.oficioRepository.findAll();
    }

    @Override
    public GenericResponse<DigitizationGetFilesResponse> obtenerArchivosSobre(TokenInfo tokenInfo, ActaBean actaBean,
            String tipoSobre) {
        GenericResponse<DigitizationGetFilesResponse> response = new GenericResponse<>();
        DigitizationGetFilesResponse data = new DigitizationGetFilesResponse();

        try {
            Archivo archivoEscrutinio = null;
            Archivo archivoInstalacion = null;

            if (ConstantesOficio.TIPO_SOBRE_PLOMO.equalsIgnoreCase(tipoSobre)) {
                archivoEscrutinio = obtenerArchivoPorId(actaBean.getIdArchivoEscrutinio());
                archivoInstalacion = obtenerArchivoPorId(actaBean.getIdArchivoInstalacionSufragio());
            } else {
                Optional<ActaCeleste> optionalCeleste = actaCelesteRepository.findByActa_IdAndEstadoDigitalizacion(
                        actaBean.getActaId(), ConstantesEstadoActa.ESTADO_DIGTAL_1ER_CONTROL_ACEPTADA);
                if (optionalCeleste.isPresent()) {
                    ActaCeleste actaCeleste = optionalCeleste.get();
                    archivoEscrutinio = actaCeleste.getArchivoEscrutinio();
                    archivoInstalacion = actaCeleste.getArchivoInstalacionSufragio();
                }
            }

            if (archivoEscrutinio == null || archivoInstalacion == null) {
                response.setSuccess(false);
                response.setMessage("No se encontró la imagen del acta del sobre " + tipoSobre.toLowerCase() + ".");
                return response;
            }

            Resource recursoEscrutinio = this.storageService.loadFile(archivoEscrutinio.getGuid(), false);
            Resource recursoInstalacion = this.storageService.loadFile(archivoInstalacion.getGuid(), false);

            File archivoTiffEscrutinio = recursoEscrutinio.getFile();
            File archivoTiffInstalacion = recursoInstalacion.getFile();

            String nombrePdfEscrutinio = archivoEscrutinio.getNombre().replaceAll("\\.\\w+$", "") + ".pdf";
            String nombrePdfInstalacion = archivoInstalacion.getNombre().replaceAll("\\.\\w+$", "") + ".pdf";

            File pdfEscrutinio = storageService.convertTIFFToPDF(archivoTiffEscrutinio, nombrePdfEscrutinio);
            File pdfInstalacion = storageService.convertTIFFToPDF(archivoTiffInstalacion, nombrePdfInstalacion);

            String base64Escrutinio = Base64.getEncoder().encodeToString(Files.readAllBytes(pdfEscrutinio.toPath()));
            String base64Instalacion = Base64.getEncoder().encodeToString(Files.readAllBytes(pdfInstalacion.toPath()));

            data.setActa1File(base64Escrutinio);
            data.setActa2File(base64Instalacion);

            response.setSuccess(true);
            response.setData(data);
            return response;
        } catch (Exception e) {
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();
            response.setSuccess(false);
            response.setMessage("Error al obtener archivos del sobre: " + e.getMessage());
            return response;
        }
    }

    private Archivo obtenerArchivoPorId(String idArchivo) {
        if (idArchivo == null)
            return null;
        try {
            return archivoRepository.findById(Long.valueOf(idArchivo)).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public GenericResponse<Boolean> transmitirOficio(Long idActa, String proceso, TransmisionNacionEnum estadoEnum,
            String usuario) {
        GenericResponse<Boolean> response = new GenericResponse<>();
        try {
            Optional<DetActaOficio> detOficioOpt = detActaOficioRepository
                    .findFirstByActa_IdOrderByFechaCreacionDesc(idActa);

            if (detOficioOpt.isEmpty()) {
                response.setSuccess(false);
                response.setData(false);
                response.setMessage("No se encontró un oficio asociado al acta con ID: " + idActa);
                return response;
            }

            Oficio oficio = detOficioOpt.get().getOficio();
            if (oficio == null) {
                response.setSuccess(false);
                response.setData(false);
                response.setMessage("No se encontró un oficio asociado al acta con ID: " + idActa);
                return response;
            }

            if (oficio.getEstado().equals(ConstantesEstadoActa.ESTADO_ACTA_ENVIADA_A_JEE)) {
                response.setSuccess(false);
                response.setData(false);
                response.setMessage("El oficio ya se encuentra en estado Enviado.");
                return response;
            }

            Acta actaPloma = detOficioOpt.get().getActa();
            if (actaPloma == null) {
                response.setSuccess(false);
                response.setData(false);
                response.setMessage("No se encontró acta asociado con ID: " + idActa);
                return response;
            }

            if (actaPloma.getEstadoActa().equals(ConstantesEstadoActa.ESTADO_ACTA_ENVIADA_A_JEE)) {
                response.setSuccess(false);
                response.setData(false);
                response.setMessage("La acta ya se encuentra en estado Enviado.");
                return response;
            }

            Date fechaActual = new Date();

            oficio.setEstado(ConstantesEstadoActa.ESTADO_ACTA_ENVIADA_A_JEE);
            oficio.setFechaEnvio(fechaActual);
            oficio.setFechaModificacion(fechaActual);
            oficio.setUsuarioModificacion(usuario);
            oficioRepository.save(oficio);

            actaPloma.setEstadoActa(ConstantesEstadoActa.ESTADO_ACTA_ENVIADA_A_JEE);
            actaPloma.setFechaModificacion(fechaActual);
            actaPloma.setUsuarioModificacion(usuario);
            actaRepository.save(actaPloma);

            actaTransmisionNacionStrategyService.sincronizar(idActa, proceso, estadoEnum, usuario);

            response.setSuccess(true);
            response.setData(true);
            response.setMessage("El oficio fue transmitido exitosamente a Nación.");

        } catch (Exception ex) {
            response.setSuccess(false);
            response.setData(false);
            response.setMessage("Ocurrió un error al transmitir el oficio: " + ex.getMessage());
        }

        return response;
    }

    @Override
    public GenericResponse<Object> verificarDocumentoEnvio(TokenInfo tokenInfo, ActaBean actaBean,
            String tipoDocumento) {
        try {
            if (actaBean == null || actaBean.getActaId() == null) {
                return new GenericResponse<>(false, "Datos de acta no válidos.");
            }

            if (tipoDocumento.equals(ConstantesOficio.TIPO_DOCUMENTO_OFICIO)) {
                return verificarOficio(actaBean.getActaId());
            } else if (tipoDocumento.equals(ConstantesOficio.TIPO_DOCUMENTO_CARGO)) {
                return verificarCargo(actaBean.getActaId());
            }

            return new GenericResponse<>(false, "Tipo de documento no válido.");
        } catch (Exception e) {
            return new GenericResponse<>(false, "Ocurrió un error interno. Contacte al administrador.");
        }
    }

    private GenericResponse<Object> verificarOficio(Long actaId) {
        return detActaOficioRepository.findFirstByActa_IdOrderByFechaCreacionDesc(actaId).map(det -> det.getOficio())
                .filter(ofi -> ofi != null && ofi.getArchivo() != null)
                .map(ofi -> cargarArchivoBase64(ofi.getArchivo().getId(), "No se encontró el archivo del oficio."))
                .orElse(new GenericResponse<>(false, "No se encontró un oficio generado."));
    }

    private GenericResponse<Object> verificarCargo(Long actaId) {
        return detActaFormatoRepository.findByActa_Id(actaId).stream()
                .filter(daf -> daf.getCabActaFormato() != null && daf.getCabActaFormato().getFormato() != null
                        && ConstantesCatalogo.N_CODIGO_CARGO_ENTREGA_ENVIO_JEE
                                .equals(daf.getCabActaFormato().getFormato().getTipoFormato())
                        && ConstantesComunes.ACTIVO.equals(daf.getActivo()))
                .findFirst().map(daf -> daf.getCabActaFormato().getArchivoFormatoPdf())
                .filter(archivo -> archivo != null)
                .map(archivo -> cargarArchivoBase64(archivo.getId(), "No se encontró el archivo del cargo de entrega."))
                .orElse(new GenericResponse<>(false, "No se encontró un cargo de entrega generado."));
    }

    private GenericResponse<Object> cargarArchivoBase64(Long archivoId, String errorMsg) {
        Archivo archivo = archivoRepository.findById(archivoId).orElse(null);
        if (archivo != null) {
            try {
                String base64 = OficioUtils.convertToBase64(PathUtils.normalizePath(this.uploadDir, archivo.getGuid()));
                return new GenericResponse<>(true, archivo.getNombre(), base64);
            } catch (IOException e) {
                logger.error("Error al convertir archivo a Base64", e);
                return new GenericResponse<>(false, "Error al convertir archivo a Base64.");
            }
        } else {
            return new GenericResponse<>(false, errorMsg);
        }
    }

    @Override
    public List<SeguimientoOficioDTO> obtenerSeguimiento(TokenInfo tokenInfo) {
        List<SeguimientoOficioDTO> lista = new ArrayList<>();

        try {
            CentroComputo cc = obtenerCentroComputo(tokenInfo);
            List<Oficio> oficios = oficioRepository.findByCentroComputo(cc.getId().intValue());

            for (Oficio oficio : oficios) {
                List<DetActaOficio> detalles = detActaOficioRepository.findByOficio_Id(oficio.getId());

                if (detalles.isEmpty())
                    continue;

                DetActaOficio detalle = detalles.get(0);

                SeguimientoOficioDTO seguimiento = new SeguimientoOficioDTO();
                seguimiento.setIdOficio(oficio.getId().longValue());
                seguimiento.setNumeroficio(oficio.getNombreOficio());
                seguimiento.setEstadoOficio(oficio.getEstado());
                seguimiento.setFechaEnvio(Optional.ofNullable(oficio.getFechaEnvio()).orElse(null));
                seguimiento.setFechaRespuesta(Optional.ofNullable(oficio.getFechaRespuesta()).orElse(null));

                seguimiento.setNumeroResolucion(Optional.ofNullable(detalle.getNumeroResolucionJNE()).orElse(""));
                seguimiento.setNumeroExpediente(Optional.ofNullable(detalle.getNumeroExpedienteJNE()).orElse(""));
                seguimiento.setArchivoJNE(detalle.getArchivoJNE());

                Acta acta = detalle.getActa();
                if (acta != null && acta.getMesa() != null) {
                    String mesaCodigo = acta.getMesa().getCodigo();
                    String numeroPloma = mesaCodigo + "-" + acta.getNumeroCopia()
                            + Optional.ofNullable(acta.getDigitoChequeoEscrutinio()).orElse("");
                    seguimiento.setActaPlomaId(acta.getId());
                    seguimiento.setIdArchivoEscrutinio(acta.getArchivoEscrutinio().getId().toString());
                    seguimiento
                            .setIdArchivoInstalacionSufragio(acta.getArchivoInstalacionSufragio().getId().toString());
                    seguimiento.setNumeroActaPloma(numeroPloma);

                    DetTipoEleccionDocumentoElectoral eleccion = detTipoEleccionDocumentoElectoralService
                            .findByCopia(acta.getNumeroCopia());
                    if (eleccion != null && eleccion.getEleccion() != null) {
                        seguimiento.setEleccion(eleccion.getEleccion().getNombre());
                    }
                }

                ActaCeleste celeste = detalle.getActaCeleste();
                if (celeste != null) {
                    String numeroCeleste = (acta != null && acta.getMesa() != null ? acta.getMesa().getCodigo() : "")
                            + "-" + celeste.getNumeroCopia()
                            + Optional.ofNullable(celeste.getDigitoChequeoEscrutinio()).orElse("");
                    seguimiento.setActaCelesteId(celeste.getId());
                    seguimiento.setNumeroActaCeleste(numeroCeleste);
                }

                TabResolucion resolucion = detalle.getResolucion();
                if (resolucion != null) {
                    seguimiento.setIdResolucion(resolucion.getId());
                }
                lista.add(seguimiento);
            }
            return lista;
        } catch (Exception e) {
            logger.error("Error en obtenerSeguimiento", e);
            return lista;
        }
    }

    private CentroComputo obtenerCentroComputo(TokenInfo tokenInfo) {
        String codigoCC = tokenInfo.getCodigoCentroComputo();

        return centroComputoService.findByCodigo(codigoCC).orElseThrow(
                () -> new IllegalArgumentException("No se encontró centro de cómputo con código: " + codigoCC));
    }
}
