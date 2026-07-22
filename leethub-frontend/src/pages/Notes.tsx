import React, { useEffect, useState } from 'react';
import { notesApi } from '../api/notes';
import type { Note } from '../types';
import {
  PencilIcon,
  TrashIcon,
  DocumentTextIcon
} from '@heroicons/react/24/outline';
import toast from 'react-hot-toast';

const NOTE_TYPES = ['', 'PERSONAL', 'INTERVIEW', 'REVISION'];

const NOTE_TYPE_COLOR: Record<string, string> = {
  PERSONAL:  'bg-blue-500/10 text-blue-400',
  INTERVIEW: 'bg-purple-500/10 text-purple-400',
  REVISION:  'bg-orange-500/10 text-orange-400',
};

export const Notes: React.FC = () => {
  const [notes, setNotes]           = useState<Note[]>([]);
  const [loading, setLoading]       = useState(true);
  const [noteType, setNoteType]     = useState('');
  const [editing, setEditing]       = useState<Note | null>(null);
  const [editContent, setEditContent] = useState('');
  const [saving, setSaving]         = useState(false);


  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    notesApi.list({ noteType: noteType || undefined, size: 50 })
      .then(res => { if (!cancelled) setNotes(res.content ?? []); })
      .catch(e => console.error(e))
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [noteType]);

  const startEdit = (note: Note) => { setEditing(note); setEditContent(note.content); };
  const cancelEdit = () => { setEditing(null); setEditContent(''); };

  const saveEdit = async () => {
    if (!editing) return;
    setSaving(true);
    try {
      const updated = await notesApi.update(editing.id, { content: editContent });
      setNotes(prev => prev.map(n => n.id === editing.id ? updated : n));
      cancelEdit();
      toast.success('Note saved');
    } catch {
      toast.error('Failed to save note');
    } finally {
      setSaving(false);
    }
  };

  const deleteNote = async (id: number) => {
    if (!confirm('Delete this note?')) return;
    try {
      await notesApi.delete(id);
      setNotes(prev => prev.filter(n => n.id !== id));
      toast.success('Note deleted');
    } catch {
      toast.error('Failed to delete note');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Notes</h1>
          <p className="mt-1 text-sm text-gray-400">Your personal annotations and revision cards.</p>
        </div>
      </div>

      {/* Type filter */}
      <div className="flex gap-2 flex-wrap">
        {NOTE_TYPES.map(t => (
          <button
            key={t}
            onClick={() => setNoteType(t)}
            className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              noteType === t
                ? 'bg-emerald-600 text-white'
                : 'bg-gray-800 text-gray-400 hover:bg-gray-700 hover:text-white'
            }`}
          >
            {t || 'All'}
          </button>
        ))}
      </div>

      {/* Notes grid */}
      {loading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-40 animate-pulse rounded-xl bg-gray-800" />
          ))}
        </div>
      ) : notes.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <DocumentTextIcon className="h-12 w-12 text-gray-700 mb-4" />
          <p className="text-gray-400 text-sm">No notes yet.</p>
          <p className="text-gray-600 text-xs mt-1">Notes are created from the Problem Detail page.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {notes.map(note => (
            <div key={note.id} className="group rounded-xl border border-gray-800 bg-gray-900 p-4 hover:border-gray-700 transition-colors flex flex-col">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p className="text-xs font-semibold text-gray-500 mb-1">{note.problemTitle || 'General Note'}</p>
                  {note.noteType && (
                    <span className={`inline-block rounded-full px-2 py-0.5 text-[10px] font-semibold ${NOTE_TYPE_COLOR[note.noteType] ?? 'text-gray-400 bg-gray-800'}`}>
                      {note.noteType}
                    </span>
                  )}
                </div>
                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button onClick={() => startEdit(note)} className="p-1 text-gray-500 hover:text-white transition-colors">
                    <PencilIcon className="h-3.5 w-3.5" />
                  </button>
                  <button onClick={() => deleteNote(note.id)} className="p-1 text-gray-500 hover:text-red-400 transition-colors">
                    <TrashIcon className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>

              {editing?.id === note.id ? (
                <div className="flex flex-col gap-2 flex-1">
                  <textarea
                    value={editContent}
                    onChange={e => setEditContent(e.target.value)}
                    className="flex-1 min-h-[120px] w-full rounded-md border border-gray-700 bg-gray-800 p-2 text-xs text-gray-200 focus:border-emerald-500 focus:outline-none resize-none"
                  />
                  <div className="flex gap-2">
                    <button onClick={saveEdit} disabled={saving}
                      className="flex-1 rounded-md bg-emerald-600 py-1.5 text-xs font-medium text-white hover:bg-emerald-700 disabled:opacity-50 transition-colors">
                      {saving ? 'Saving…' : 'Save'}
                    </button>
                    <button onClick={cancelEdit}
                      className="rounded-md border border-gray-700 px-3 py-1.5 text-xs text-gray-400 hover:text-white transition-colors">
                      Cancel
                    </button>
                  </div>
                </div>
              ) : (
                <p className="text-xs text-gray-400 leading-relaxed flex-1 overflow-hidden line-clamp-6 whitespace-pre-wrap">
                  {note.content}
                </p>
              )}

              <p className="mt-3 text-[10px] text-gray-600">
                {new Date(note.updatedAt).toLocaleDateString()}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
