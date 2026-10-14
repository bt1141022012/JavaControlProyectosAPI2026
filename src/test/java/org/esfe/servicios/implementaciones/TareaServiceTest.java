package org.esfe.servicios.implementaciones;

import org.esfe.dtos.tarea.TareaCambiarEstado;
import org.esfe.dtos.tarea.TareaGuardar;
import org.esfe.dtos.tarea.TareaModificar;
import org.esfe.dtos.tarea.TareaSalida;
import org.esfe.modelos.Proyecto;
import org.esfe.modelos.Tarea;
import org.esfe.repositorios.ITareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private ITareaRepository tareaRepository;

    @InjectMocks
    private TareaService tareaService;

    private final ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tareaService, "tareaRepository", tareaRepository);
        ReflectionTestUtils.setField(tareaService, "modelMapper", modelMapper);
    }

    /**
     * Caso de prueba: debe listar todas las tareas registradas en el sistema.
     */
    @Test
    @DisplayName("Debe listar todas las tareas")
    void obtenerTodos() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(5);

        Tarea tarea1 = new Tarea();
        tarea1.setId(1);
        tarea1.setNombre("Diseño de interfaz");
        tarea1.setDescripcion("Crear mockups iniciales");
        tarea1.setDuracion("4h");
        tarea1.setEstado(Tarea.Status.APROBADA);
        tarea1.setProyecto(proyecto);

        Tarea tarea2 = new Tarea();
        tarea2.setId(2);
        tarea2.setNombre("Implementación de login");
        tarea2.setDescripcion("Desarrollar autenticación");
        tarea2.setDuracion("8h");
        tarea2.setEstado(Tarea.Status.PROCESO);
        tarea2.setProyecto(proyecto);

        when(tareaRepository.findAll()).thenReturn(List.of(tarea1, tarea2));

        List<TareaSalida> resultado = tareaService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Diseño de interfaz", resultado.get(0).getNombre());
        assertEquals("Implementación de login", resultado.get(1).getNombre());
        verify(tareaRepository).findAll();
    }

    /**
     * Caso de prueba: debe devolver una página de tareas según el tamaño de página solicitado.
     */
    @Test
    @DisplayName("Debe obtener las tareas paginadas")
    void obtenerTodosPaginados() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(6);

        Tarea tarea = new Tarea();
        tarea.setId(3);
        tarea.setNombre("Pruebas unitarias");
        tarea.setDescripcion("Validar funcionalidades principales");
        tarea.setDuracion("6h");
        tarea.setEstado(Tarea.Status.REVISIÓN);
        tarea.setProyecto(proyecto);

        Page<Tarea> pagina = new PageImpl<>(List.of(tarea), PageRequest.of(0, 10), 1);
        when(tareaRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(pagina);

        Page<TareaSalida> resultado = tareaService.obtenerTodosPaginados(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals("Pruebas unitarias", resultado.getContent().get(0).getNombre());
        verify(tareaRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    /**
     * Caso de prueba: debe consultar una tarea por su id cuando existe en la base de datos.
     */
    @Test
    @DisplayName("Debe buscar una tarea por id")
    void obtenerPorId() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(7);

        Tarea tarea = new Tarea();
        tarea.setId(4);
        tarea.setNombre("Documentación");
        tarea.setDescripcion("Preparar manual del sistema");
        tarea.setDuracion("3h");
        tarea.setEstado(Tarea.Status.FINALIZADA);
        tarea.setProyecto(proyecto);

        when(tareaRepository.findById(4)).thenReturn(Optional.of(tarea));

        TareaSalida resultado = tareaService.obtenerPorId(4);

        assertNotNull(resultado);
        assertEquals(4, resultado.getId());
        assertEquals("Documentación", resultado.getNombre());
        assertEquals("FINALIZADA", resultado.getEstado());
        verify(tareaRepository).findById(4);
    }

    /**
     * Caso de prueba: debe devolver las tareas asociadas a un proyecto específico.
     */
    @Test
    @DisplayName("Debe obtener tareas por proyecto")
    void obtenerPorProyectoId() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(8);

        Tarea tarea1 = new Tarea();
        tarea1.setId(5);
        tarea1.setNombre("Backend");
        tarea1.setDescripcion("Servicios REST");
        tarea1.setDuracion("10h");
        tarea1.setEstado(Tarea.Status.PROCESO);
        tarea1.setProyecto(proyecto);

        when(tareaRepository.findByProyectoId(8)).thenReturn(List.of(tarea1));

        List<TareaSalida> resultado = tareaService.obtenerPorProyectoId(8);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Backend", resultado.get(0).getNombre());
        verify(tareaRepository).findByProyectoId(8);
    }

    /**
     * Caso de prueba: debe crear una tarea y asignar el estado inicial APROBADA por defecto.
     */
    @Test
    @DisplayName("Debe crear una tarea con estado aprobado")
    void crear() {
        TareaGuardar tareaGuardar = new TareaGuardar();
        tareaGuardar.setNombre("Revisión de requisitos");
        tareaGuardar.setDescripcion("Validar alcance del proyecto");
        tareaGuardar.setDuracion("5h");
        tareaGuardar.setProyectoId(9);

        Tarea tareaGuardada = new Tarea();
        tareaGuardada.setId(6);
        tareaGuardada.setNombre("Revisión de requisitos");
        tareaGuardada.setDescripcion("Validar alcance del proyecto");
        tareaGuardada.setDuracion("5h");
        tareaGuardada.setEstado(Tarea.Status.APROBADA);

        when(tareaRepository.save(any(Tarea.class))).thenReturn(tareaGuardada);

        TareaSalida resultado = tareaService.crear(tareaGuardar);

        assertNotNull(resultado);
        assertEquals(6, resultado.getId());
        assertEquals("Revisión de requisitos", resultado.getNombre());
        assertEquals("APROBADA", resultado.getEstado());

        ArgumentCaptor<Tarea> captor = ArgumentCaptor.forClass(Tarea.class);
        verify(tareaRepository).save(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(Tarea.Status.APROBADA, captor.getValue().getEstado());
    }

    /**
     * Caso de prueba: debe editar una tarea con los valores modificados y devolver la versión actualizada.
     */
    @Test
    @DisplayName("Debe editar una tarea")
    void editar() {
        TareaModificar tareaModificar = new TareaModificar();
        tareaModificar.setId(7);
        tareaModificar.setNombre("Actualizar documentación");
        tareaModificar.setDescripcion("Corregir guías de uso");
        tareaModificar.setDuracion("7h");
        tareaModificar.setEstado("PROCESO");
        tareaModificar.setProyectoId(10);

        Tarea tareaActualizada = new Tarea();
        tareaActualizada.setId(7);
        tareaActualizada.setNombre("Actualizar documentación");
        tareaActualizada.setDescripcion("Corregir guías de uso");
        tareaActualizada.setDuracion("7h");
        tareaActualizada.setEstado(Tarea.Status.PROCESO);

        when(tareaRepository.save(any(Tarea.class))).thenReturn(tareaActualizada);

        TareaSalida resultado = tareaService.editar(tareaModificar);

        assertNotNull(resultado);
        assertEquals(7, resultado.getId());
        assertEquals("Actualizar documentación", resultado.getNombre());
        assertEquals("PROCESO", resultado.getEstado());
        verify(tareaRepository).save(any(Tarea.class));
    }

    /**
     * Caso de prueba: debe cambiar el estado de la tarea a uno nuevo validado por el enum del dominio.
     */
    @Test
    @DisplayName("Debe cambiar el estado de una tarea")
    void cambiarEstado() {
        Tarea tarea = new Tarea();
        tarea.setId(8);
        tarea.setNombre("Validación final");
        tarea.setDescripcion("Verificar despliegue");
        tarea.setDuracion("2h");
        tarea.setEstado(Tarea.Status.PROCESO);

        TareaCambiarEstado tareaCambiarEstado = new TareaCambiarEstado();
        tareaCambiarEstado.setId(8);
        tareaCambiarEstado.setEstado("FINALIZADA");

        when(tareaRepository.findById(8)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        TareaSalida resultado = tareaService.cambiarEstado(tareaCambiarEstado);

        assertNotNull(resultado);
        assertEquals("FINALIZADA", resultado.getEstado());
        verify(tareaRepository).findById(8);
        verify(tareaRepository).save(any(Tarea.class));
    }

    /**
     * Caso de prueba: debe invocar la eliminación de una tarea por id sin errores.
     */
    @Test
    @DisplayName("Debe eliminar una tarea por id")
    void eliminarPorId() {
        doNothing().when(tareaRepository).deleteById(11);

        assertDoesNotThrow(() -> tareaService.eliminarPorId(11));
        verify(tareaRepository).deleteById(11);
    }
}
