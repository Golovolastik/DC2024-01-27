package by.haritonenko.jpa.util.dto.note.response;

import by.haritonenko.jpa.dto.response.NoteResponseDto;
import by.haritonenko.jpa.util.TestBuilder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

@AllArgsConstructor
@NoArgsConstructor(staticName = "note")
@With
public class CreateNoteResponseTestBuilder implements TestBuilder<NoteResponseDto> {

    private Long id = 2L;
    private Long storyId = 1L;
    private String content = "createdNote";

    @Override
    public NoteResponseDto build() {
        NoteResponseDto note = new NoteResponseDto();

        note.setId(id);
        note.setContent(content);
        note.setStoryId(storyId);

        return note;
    }
}