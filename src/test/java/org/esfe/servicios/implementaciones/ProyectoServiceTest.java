package org.esfe.servicios.implementaciones;

import org.esfe.dtos.categoria.CategoriaSalida;
import org.esfe.dtos.proyecto.ProyectoGuardar;
import org.esfe.dtos.proyecto.ProyectoModificar;
import org.esfe.dtos.proyecto.ProyectoSalida;
import org.esfe.modelos.Categoria;
import org.esfe.modelos.Proyecto;
import org.esfe.repositorios.IProyectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private IProyectoRepository proyectoRepository;

    @InjectMocks
    private ProyectoService proyectoService;

    private final ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(proyectoService, "proyectoRepository", proyectoRepository);
        ReflectionTestUtils.setField(proyectoService, "modelMapper", modelMapper);
    }

    /**
     * Caso de prueba: debe devolver todos los proyectos almacenados en el repositorio.
     */
    @Test
    @DisplayName("Debe listar todos los proyectos")
    void obtenerTodos() {
        Categoria categoria = new Categoria();
        categoria.setId(1);
        categoria.setNombre("Desarrollo");

        Proyecto proyecto1 = new Proyecto();
        proyecto1.setId(10);
        proyecto1.setTitulo("Sistema de ventas");
        proyecto1.setDescripcion("Proyecto para controlar ventas");
        proyecto1.setFechaInicio(LocalDate.of(2025, 1, 10));
        proyecto1.setFechaFin(LocalDate.of(2025, 6, 30));
        proyecto1.setCategoria(categoria);

        Proyecto proyecto2 = new Proyecto();
        proyecto2.setId(11);
        proyecto2.setTitulo("Portal web");
        proyecto2.setDescripcion("Proyecto para sitio institucional");
        proyecto2.setFechaInicio(LocalDate.of(2025, 2, 1));
        proyecto2.setFechaFin(LocalDate.of(2025, 8, 1));
        proyecto2.setCategoria(categoria);

        when(proyectoRepository.findAll()).thenReturn(List.of(proyecto1, proyecto2));

        List<ProyectoSalida> resultado = proyectoService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Sistema de ventas", resultado.get(0).getTitulo());
        assertEquals("Portal web", resultado.get(1).getTitulo());
        verify(proyectoRepository).findAll();
    }

    /**
     * Caso de prueba: debe devolver una página de proyectos con la paginación indicada.
     */
    @Test
    @DisplayName("Debe obtener los proyectos paginados")
    void obtenerTodosPaginados() {
        Categoria categoria = new Categoria();
        categoria.setId(1);
        categoria.setNombre("Desarrollo");

        Proyecto proyecto = new Proyecto();
        proyecto.setId(12);
        proyecto.setTitulo("API de gestión");
        proyecto.setDescripcion("Backend del sistema");
        proyecto.setFechaInicio(LocalDate.of(2025, 3, 1));
        proyecto.setFechaFin(LocalDate.of(2025, 9, 30));
        proyecto.setCategoria(categoria);

        Page<Proyecto> pagina = new PageImpl<>(List.of(proyecto), PageRequest.of(0, 10), 1);
        when(proyectoRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(pagina);

        Page<ProyectoSalida> resultado = proyectoService.obtenerTodosPaginados(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals("API de gestión", resultado.getContent().get(0).getTitulo());
        verify(proyectoRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    /**
     * Caso de prueba: debe devolver un proyecto cuando el identificador existe en el repositorio.
     */
    @Test
    @DisplayName("Debe buscar un proyecto por id cuando existe")
    void obtenerPorId() {
        Categoria categoria = new Categoria();
        categoria.setId(2);
        categoria.setNombre("Investigación");

        Proyecto proyecto = new Proyecto();
        proyecto.setId(15);
        proyecto.setTitulo("Monitoreo");
        proyecto.setDescripcion("Proyecto de monitoreo");
        proyecto.setFechaInicio(LocalDate.of(2025, 4, 1));
        proyecto.setFechaFin(LocalDate.of(2025, 12, 1));
        proyecto.setCategoria(categoria);

        when(proyectoRepository.findById(15)).thenReturn(Optional.of(proyecto));

        ProyectoSalida resultado = proyectoService.obtenerPorId(15);

        assertNotNull(resultado);
        assertEquals(15, resultado.getId());
        assertEquals("Monitoreo", resultado.getTitulo());
        assertNotNull(resultado.getCategoria());
        assertEquals("Investigación", resultado.getCategoria().getNombre());
        verify(proyectoRepository).findById(15);
    }

    /**
     * Caso de prueba: debe devolver null cuando el proyecto solicitado no existe.
     */
    @Test
    @DisplayName("Debe devolver null cuando el proyecto no existe")
    void obtenerPorIdCuandoNoExiste() {
        when(proyectoRepository.findById(999)).thenReturn(Optional.empty());

        ProyectoSalida resultado = proyectoService.obtenerPorId(999);

        assertNull(resultado);
        verify(proyectoRepository).findById(999);
    }

    /**
     * Caso de prueba: debe crear un proyecto con la información capturada en el DTO y devolver el registro generado.
     */
    @Test
    @DisplayName("Debe crear un proyecto")
    void crear() {
        ProyectoGuardar proyectoGuardar = new ProyectoGuardar(
                "Plataforma interna",
                "Sistema para gestión interna",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 10, 15),
                3
        );

        Proyecto proyectoCreado = new Proyecto();
        proyectoCreado.setId(22);
        proyectoCreado.setTitulo("Plataforma interna");
        proyectoCreado.setDescripcion("Sistema para gestión interna");
        proyectoCreado.setFechaInicio(LocalDate.of(2025, 5, 1));
        proyectoCreado.setFechaFin(LocalDate.of(2025, 10, 15));

        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(proyectoCreado);

        ProyectoSalida resultado = proyectoService.crear(proyectoGuardar);

        assertNotNull(resultado);
        assertEquals(22, resultado.getId());
        assertEquals("Plataforma interna", resultado.getTitulo());
        verify(proyectoRepository).save(any(Proyecto.class));
    }

    /**
     * Caso de prueba: debe actualizar un proyecto con los nuevos datos recibidos en el DTO.
     */
    @Test
    @DisplayName("Debe editar un proyecto")
    void editar() {
        ProyectoModificar proyectoModificar = new ProyectoModificar(
                30,
                "Dashboard actualizado",
                "Versión mejorada del dashboard",
                LocalDate.of(2025, 6, 10),
                LocalDate.of(2025, 12, 15),
                4
        );

        Proyecto proyectoActualizado = new Proyecto();
        proyectoActualizado.setId(30);
        proyectoActualizado.setTitulo("Dashboard actualizado");
        proyectoActualizado.setDescripcion("Versión mejorada del dashboard");
        proyectoActualizado.setFechaInicio(LocalDate.of(2025, 6, 10));
        proyectoActualizado.setFechaFin(LocalDate.of(2025, 12, 15));

        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(proyectoActualizado);

        ProyectoSalida resultado = proyectoService.editar(proyectoModificar);

        assertNotNull(resultado);
        assertEquals(30, resultado.getId());
        assertEquals("Dashboard actualizado", resultado.getTitulo());
        verify(proyectoRepository).save(any(Proyecto.class));
    }

    /**
     * Caso de prueba: debe invocar la eliminación por identificación sin generar errores.
     */
    @Test
    @DisplayName("Debe eliminar un proyecto por id")
    void eliminarPorId() {
        doNothing().when(proyectoRepository).deleteById(18);

        assertDoesNotThrow(() -> proyectoService.eliminarPorId(18));
        verify(proyectoRepository).deleteById(18);
    }
}