import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import Button from '@/components/ui/Button';

describe('Button', () => {
  it('テキストを表示する', () => {
    render(<Button>クリック</Button>);
    expect(screen.getByRole('button', { name: 'クリック' })).toBeInTheDocument();
  });

  it('クリックイベントを発火する', () => {
    const onClick = vi.fn();
    render(<Button onClick={onClick}>テスト</Button>);
    fireEvent.click(screen.getByRole('button'));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('isLoading時は「読み込み中...」を表示しdisabledになる', () => {
    render(<Button isLoading>送信</Button>);
    const button = screen.getByRole('button');
    expect(button).toHaveTextContent('読み込み中...');
    expect(button).toBeDisabled();
  });

  it('disabled時はクリックできない', () => {
    const onClick = vi.fn();
    render(<Button disabled onClick={onClick}>テスト</Button>);
    fireEvent.click(screen.getByRole('button'));
    expect(onClick).not.toHaveBeenCalled();
  });
});
