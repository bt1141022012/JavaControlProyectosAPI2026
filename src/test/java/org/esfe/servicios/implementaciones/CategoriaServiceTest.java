package org.esfe.servicios.implementaciones;

import org.esfe.dtos.categoria.CategoriaGuardar;
import org.esfe.dtos.categoria.CategoriaModificar;
import org.esfe.dtos.categoria.CategoriaSalida;
import org.esfe.modelos.Categoria;
import org.esfe.repositorios.ICategoriaRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private ICategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private final ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(categoriaService, "categoriaRepository", categoriaRepository);
        ReflectionTestUtils.setField(categoriaService, "modelMapper", modelMapper);
    }

    /**
     * Caso de prueba: debe devolver todas las categorías registradas en la base de datos.
     */
    @Test
    @DisplayName("Debe listar todas las categorías")
    void obtenerTodos() {
        Categoria categoria1 = new Categoria();
        categoria1.setId(1);
        categoria1.setNombre("Desarrollo");

        Categoria categoria2 = new Categoria();
        categoria2.setId(2);
        categoria2.setNombre("Infraestructura");

        when(categoriaRepository.findAll()).thenReturn(List.of(categoria1, categoria2));

        List<CategoriaSalida> resultado = categoriaService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Desarrollo", resultado.get(0).getNombre());
        assertEquals("Infraestructura", resultado.get(1).getNombre());
        verify(categoriaRepository).findAll();
    }

    /**
     * Caso de prueba: debe devolver una página de categorías según la paginación solicitada.
     */
    @Test
    @DisplayName("Debe obtener las categorías paginadas")
    void obtenerTodosPaginados() {
        Categoria categoria = new Categoria();
        categoria.setId(1);
        categoria.setNombre("Diseño");

        Page<Categoria> pagina = new PageImpl<>(List.of(categoria), PageRequest.of(0, 10), 1);
        when(categoriaRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(pagina);

        Page<CategoriaSalida> resultado = categoriaService.obtenerTodosPaginados(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals("Diseño", resultado.getContent().get(0).getNombre());
        verify(categoriaRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    /**
     * Caso de prueba: debe buscar una categoría por su identificador cuando existe.
     */
    @Test
    @DisplayName("Debe consultar una categoría por id")
    void obtenerPorId() {
        Categoria categoria = new Categoria();
        categoria.setId(5);
        categoria.setNombre("Soporte");

        when(categoriaRepository.findById(5)).thenReturn(Optional.of(categoria));

        CategoriaSalida resultado = categoriaService.obtenerPorId(5);

        assertNotNull(resultado);
        assertEquals(5, resultado.getId());
        assertEquals("Soporte", resultado.getNombre());
        verify(categoriaRepository).findById(5);
    }

    /**
     * Caso de prueba: debe crear una categoría con el nombre enviado en el DTO y devolver el registro persistido.
     */
    @Test
    @DisplayName("Debe crear una categoría")
    void crear() {
        CategoriaGuardar categoriaGuardar = new CategoriaGuardar("QA");
        Categoria categoriaGuardada = new Categoria();
        categoriaGuardada.setId(7);
        categoriaGuardada.setNombre("QA");

        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaGuardada);

        CategoriaSalida resultado = categoriaService.crear(categoriaGuardar);

        assertNotNull(resultado);
        assertEquals(7, resultado.getId());
        assertEquals("QA", resultado.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    /**
     * Caso de prueba: debe actualizar una categoría existente con los valores modificados.
     */
    @Test
    @DisplayName("Debe editar una categoría")
    void editar() {
        CategoriaModificar categoriaModificar = new CategoriaModificar(8, "Seguridad");
        Categoria categoriaActualizada = new Categoria();
        categoriaActualizada.setId(8);
        categoriaActualizada.setNombre("Seguridad");

        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaActualizada);

        CategoriaSalida resultado = categoriaService.editar(categoriaModificar);

        assertNotNull(resultado);
        assertEquals(8, resultado.getId());
        assertEquals("Seguridad", resultado.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    /**
     * Caso de prueba: debe invocar la eliminación por identificador sin lanzar errores.
     */
    @Test
    @DisplayName("Debe eliminar una categoría por id")
    void eliminarPorId() {
        doNothing().when(categoriaRepository).deleteById(3);

        assertDoesNotThrow(() -> categoriaService.eliminarPorId(3));
        verify(categoriaRepository).deleteById(3);
    }
}