package com.allpowerful.backend.memo;

import com.allpowerful.backend.common.AppException;
import com.allpowerful.backend.common.DomainEventPublisher;
import com.allpowerful.backend.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoService {
    private final MemoRepository memoRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public List<MemoDtos.MemoResponse> list(Long userId) {
        return memoRepository.findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MemoDtos.MemoResponse> trash(Long userId) {
        return memoRepository.findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public MemoDtos.MemoResponse create(Long userId, MemoDtos.MemoRequest req) {
        Memo memo = new Memo();
        patch(memo, userId, req);
        memo.setCreatedAt(LocalDateTime.now());
        memo.setUpdatedAt(LocalDateTime.now());
        memoRepository.save(memo);
        if (memo.getRemindAt() != null) {
            domainEventPublisher.publish(KafkaTopics.MEMO_REMINDER_CHANGED, "{\"memoId\":" + memo.getId() + "}");
        }
        return toResponse(memo);
    }

    @Transactional
    public MemoDtos.MemoResponse update(Long userId, Long id, MemoDtos.MemoRequest req) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new AppException("备忘录不存在"));
        if (!memo.getUserId().equals(userId)) throw new AppException("无权限");
        patch(memo, userId, req);
        memo.setUpdatedAt(LocalDateTime.now());
        memoRepository.save(memo);
        if (memo.getRemindAt() != null) {
            domainEventPublisher.publish(KafkaTopics.MEMO_REMINDER_CHANGED, "{\"memoId\":" + memo.getId() + "}");
        }
        return toResponse(memo);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new AppException("备忘录不存在"));
        if (!memo.getUserId().equals(userId)) throw new AppException("无权限");
        memo.setDeletedAt(LocalDateTime.now());
        memo.setUpdatedAt(LocalDateTime.now());
        memoRepository.save(memo);
    }

    @Transactional
    public void restore(Long userId, Long id) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new AppException("备忘录不存在"));
        if (!memo.getUserId().equals(userId)) throw new AppException("无权限");
        memo.setDeletedAt(null);
        memo.setUpdatedAt(LocalDateTime.now());
        memoRepository.save(memo);
    }

    @Transactional
    public void deletePermanent(Long userId, Long id) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new AppException("备忘录不存在"));
        if (!memo.getUserId().equals(userId)) throw new AppException("无权限");
        memoRepository.delete(memo);
    }

    private void patch(Memo memo, Long userId, MemoDtos.MemoRequest req) {
        memo.setUserId(userId);
        memo.setTitle(req.title());
        memo.setContent(req.content());
        memo.setCategory(req.category());
        memo.setPinned(req.pinned());
        memo.setRemindAt(req.remindAt());
        memo.setRemindRepeat(req.remindRepeat());
        memo.getTodos().clear();
        if (req.todos() != null) {
            for (int i = 0; i < req.todos().size(); i++) {
                MemoDtos.TodoItem t = req.todos().get(i);
                MemoTodo todo = new MemoTodo();
                todo.setMemo(memo);
                todo.setText(t.text());
                todo.setDone(t.done());
                todo.setSortOrder(i);
                memo.getTodos().add(todo);
            }
        }
    }

    private MemoDtos.MemoResponse toResponse(Memo memo) {
        List<MemoDtos.TodoItem> todos = memo.getTodos().stream()
                .map(t -> new MemoDtos.TodoItem(t.getId(), t.getText(), t.getDone()))
                .toList();
        return new MemoDtos.MemoResponse(
                memo.getId(),
                memo.getTitle(),
                memo.getContent(),
                memo.getCategory(),
                memo.getPinned(),
                memo.getRemindAt(),
                memo.getRemindRepeat(),
                memo.getUpdatedAt(),
                todos
        );
    }
}
