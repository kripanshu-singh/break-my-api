# UI Context

## Theme

Dark-only workspace aesthetic. No light mode. The design language is a dark technical terminal — near-black backgrounds, layered card surfaces with subtle borders, and vibrant accent colors for interactive elements and status indicators. The dashboard should feel like a monitoring tool: data-dense, clear, and authoritative.

## Colors

| Role              | CSS Variable       | Value    |
| ----------------- | ------------------ | -------- |
| Page background   | `--bg-base`        | `#0a0a0b` |
| Surface           | `--bg-surface`     | `#141416` |
| Elevated surface  | `--bg-elevated`    | `#1c1c1f` |
| Primary text      | `--text-primary`   | `#f4f4f5` |
| Muted text        | `--text-muted`     | `#a1a1aa` |
| Primary accent    | `--accent-primary` | `#6366f1` |
| Accent hover      | `--accent-hover`   | `#818cf8` |
| Border            | `--border-default` | `#27272a` |
| Border hover      | `--border-hover`   | `#3f3f46` |
| Error             | `--state-error`    | `#ef4444` |
| Success           | `--state-success`  | `#22c55e` |
| Warning           | `--state-warning`  | `#f59e0b` |
| Critical          | `--state-critical` | `#dc2626` |

## Typography

| Role      | Font              | Variable      |
| --------- | ----------------- | ------------- |
| UI text   | Inter (Geist Sans) | `--font-sans` |
| Code/mono | JetBrains Mono    | `--font-mono` |

## Border Radius

| Context           | Class            |
| ----------------- | ---------------- |
| Inline / small UI | `rounded-md`     |
| Cards / panels    | `rounded-lg`     |
| Modals / overlays | `rounded-xl`     |

## Component Library

Built with Tailwind CSS. No external component library — use Tailwind utility classes directly. Components live in `frontend/components/`. Reusable primitives (Button, Input, Card, Badge) should be built as simple Tailwind-wrapped components without external dependencies.

## Layout Patterns

- **Landing**: centered single-column layout with input form, mode toggle (single/swagger), and submit button
- **Dashboard**: full-viewport split — left sidebar with run summary/progress, main area with streaming results list
- **Report**: centered single-column with severity-ordered findings cards, shareable URL bar at top
- **Modals**: centered overlay with backdrop blur for auth header entry and confirmation dialogs
- **Navbar**: top bar with logo, GitHub link, and status indicator

## Icons

Lucide React. Stroke-based icons only. Sizes: h-4 w-4 for inline, h-5 w-5 for buttons and interactive elements.
