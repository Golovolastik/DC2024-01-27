package by.bsuir.publisher.controllers;

import by.bsuir.publisher.dto.requests.IssueRequestDto;
import by.bsuir.publisher.dto.responses.IssueResponseDto;
import by.bsuir.publisher.exceptions.EntityExistsException;
import by.bsuir.publisher.exceptions.Messages;
import by.bsuir.publisher.exceptions.NoEntityExistsException;
import by.bsuir.publisher.services.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;

    @PostMapping
    public ResponseEntity<IssueResponseDto> create(@RequestBody IssueRequestDto issue) throws EntityExistsException {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.create(issue));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponseDto> read(@PathVariable("id") Long id) throws NoEntityExistsException {
        return ResponseEntity.status(HttpStatus.OK).body(issueService.read(id).orElseThrow(() ->
                new NoEntityExistsException(Messages.NoEntityExistsException)));
    }

    @GetMapping
    public ResponseEntity<List<IssueResponseDto>> read() {
        return ResponseEntity.status(HttpStatus.OK).body(issueService.readAll());
    }

    @PutMapping
    public ResponseEntity<IssueResponseDto> update(@RequestBody IssueRequestDto issue) throws NoEntityExistsException {
        return ResponseEntity.status(HttpStatus.OK).body(issueService.update(issue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> delete(@PathVariable("id") Long id) throws NoEntityExistsException {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(issueService.delete(id));
    }
}
