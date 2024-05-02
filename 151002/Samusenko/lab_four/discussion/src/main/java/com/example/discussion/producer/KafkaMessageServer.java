package com.example.discussion.producer;

import com.example.discussion.dao.repository.MessageRepository;
import com.example.discussion.event.*;
import com.example.discussion.model.entity.Message;
import com.example.discussion.model.entity.MessageKafka;
import com.example.discussion.model.entity.MessageState;
import com.example.discussion.model.response.MessageResponseTo;
import com.example.discussion.service.MessageService;
import com.example.discussion.service.mapper.MessageKafkaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageServer {
    public static final String IN_TOPIC = "in-topic";
    public static final String OUT_TOPIC = "out-topic";
    private final MessageKafkaMapper messageKafkaMapper;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, OutTopicEvent> kafkaTemplate;

    @KafkaListener(topics = IN_TOPIC, groupId = "group-id=#{T(java.util.UUID).randomUUID().toString()}")
    private void process(ConsumerRecord<String, InTopicEvent> record) {
        final String key = record.key();
        final InTopicEvent inTopicEvent = record.value();
        final InTopicMessage inTopicMsg = inTopicEvent.message();
        final MessageInTopicTo inTopicMessage = inTopicMsg.commentDto();
        log.info(IN_TOPIC + ": key = " + key + "; operation = " + inTopicMsg.operation() + "; data = " + inTopicMsg.commentDto());
        List<MessageOutTopicTo> resultList = switch (inTopicMsg.operation()) {
            case GET_ALL -> messageKafkaMapper.responseDtoToOutTopicDto(messageService.findAll());
            case GET_BY_ID -> getById(inTopicMessage.getId());
            case DELETE_BY_ID -> delete(inTopicMessage.getId());
            case CREATE -> create(inTopicMessage);
            case UPDATE -> update(inTopicMessage);
        };
        log.info(OUT_TOPIC + ": key = " + key + "; data = " + resultList);
        kafkaTemplate.send(
                OUT_TOPIC,
                key,
                new OutTopicEvent(
                        inTopicEvent.id(),
                        new OutTopicMessage(
                                resultList
                        )
                )
        );
    }

    private List<MessageOutTopicTo> getById(Long id) {
        MessageResponseTo responseDto;
        try {
            responseDto = messageService.findById(new BigInteger(id.toString()));
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        return Collections.singletonList(messageKafkaMapper.responseDtoToOutTopicDto(responseDto));
    }

    private List<MessageOutTopicTo> create(MessageInTopicTo dto) {
        MessageKafka newEntity = new MessageKafka(messageKafkaMapper.toEntity(dto), MessageState.APPROVE);

        try {
            return Collections.singletonList(messageKafkaMapper.entityToDto(messageRepository.save(messageKafkaMapper.toEntity(dto))));
        } catch (Exception ex) {
            newEntity.setState(MessageState.DECLINE);
            return Collections.emptyList();
        }
    }

    private List<MessageOutTopicTo> update(MessageInTopicTo dto) {
        Optional<Message> optionalNote = messageRepository.findById(new BigInteger(dto.getId().toString()));
        if (optionalNote.isEmpty()) {
            return Collections.emptyList();
        }
        final Message entity = optionalNote.get();
        final Message updated = messageKafkaMapper.partialUpdate(dto, entity);
        return Collections.singletonList(messageKafkaMapper.entityToDto(messageRepository.save(updated)));
    }

    private List<MessageOutTopicTo> delete(Long id) {
        Optional<Message> optionalNote = messageRepository.findById(new BigInteger(id.toString()));
        if (optionalNote.isEmpty()) {
            return Collections.emptyList();
        }
        final Message deleted = optionalNote.get();
        messageRepository.delete(deleted);
        return Collections.singletonList(messageKafkaMapper.entityToDto(deleted));
    }
}