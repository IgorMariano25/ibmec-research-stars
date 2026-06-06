package br.com.ibmec.researchstars.course;

import br.com.ibmec.researchstars.course.dto.CourseDto;
import br.com.ibmec.researchstars.course.dto.CreateCourseRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // GET — qualquer usuário autenticado (RF-26)
    @GetMapping
    public Page<CourseDto> listAll(
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return courseService.listAll(query, pageable);
    }

    // POST — Admin (RF-25)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> create(@Valid @RequestBody CreateCourseRequest request) {
        CourseDto created = courseService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/courses/" + created.id())).body(created);
    }

    // PATCH — Admin (RF-25)
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> update(@PathVariable Long id,
                                            @Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    // DELETE — Admin (RF-25)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
