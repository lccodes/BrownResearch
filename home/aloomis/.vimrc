call pathogen#infect()

syntax enable "Enable syntax highlighting
colorscheme solarized

if has('gui_running')
    set guioptions-=T "Disable toolbar
    set guioptions-=r "Disable scrollbar
    set guifont=Inconsolata:h14
    set background=light
else
    set background=dark
endif

set nocompatible "Disable compatibility with vi
set autoread "Automatically read a file that is changed outside of vim
set backspace=2 "Make backspace work normally
set nowrap "Disable wrapping of long lines
set sidescroll=1 "Minimal number of columns to scroll
set sidescrolloff=1 "Minimal number of columns to the left and right
set scrolloff=1 "Minimal number of lines to the top and bottom
set textwidth=80 "Set the text width for formatting operations
set formatoptions-=t "Disable automatic insertion of line breaks
set showmatch "Highlight matching brace
set matchtime=0 "Do not wait to show matching brace
set incsearch "Enable incremental search
set hlsearch "Enable search term highlighting
set noerrorbells "Disable all error bells
set novisualbell "Disable visual beeps
set cino=l1,g0,N-s "C/C++ specific formating options
set expandtab "Enable tabs as spaces
set softtabstop=4 "Set the size of inserted tabs
set shiftwidth=4 "Set the size of tabs in reindenting operations
set autoindent "Copy indentation from previous line
set cursorline "Highlight the cursor line
set number "Display line numbers
set ruler "Show cursor position
set wildmenu "Command mode tab completion options
set wildmode=list:longest "Match to longest completion option
set wildignore=*.o,moc_*.cpp "Ignore object files and generated Qt files
set lazyredraw "Do not redraw in the middle of a macro
set foldmethod=indent "Fold code based on indentation
set nofoldenable "Do not fold by default
set foldlevel=1 "Only fold one level
set diffopt+=iwhite "Ignore whitespace using vimdiff
set showmode "Show the mode on the last line

filetype plugin on "Enable filetype detection
filetype indent on "Enable filetype indentation

"Disable expandtab for makefiles
autocmd FileType make setlocal noexpandtab tabstop=4
autocmd FileType html setlocal softtabstop=2 shiftwidth=2

set completeopt=menu,menuone,longest
"set concealcursor=inv
"set conceallevel=2

let g:clang_auto_select = 2
"g:clang_complete_auto
let g:clang_complete_copen = 1
"g:clang_hl_errors
"g:clang_periodic_quickfix
let g:clang_snippets = 1
"g:clang_snippets_engine
"let g:clang_conceal_snippets = 1
"g:clang_trailing_placeholder
"g:clang_close_preview
"g:clang_exec
"g:clang_user_options
"g:clang_auto_user_options
"g:clang_compilation_database
let g:clang_use_library = 1
"g:clang_library_path
"g:clang_sort_algo
"g:clang_complete_macros
"g:clang_complete_patterns

"Change the leader key
let mapleader = ","

"Map <leader>n to disable search highlighting
nmap <silent> <leader>n :silent :nohlsearch<CR>

"Replace word under cursor with <leader>s
nnoremap <leader>s :%s/\<<C-r><C-w>\>/

"Map <leader>p to toggle paste mode
nnoremap <leader>p :set invpaste paste?<CR>

au! BufEnter *.hpp let b:fswitchdst = 'cpp'
au! BufEnter *.hpp let b:fswitchlocs = 'reg:/include/src/,reg:/include.*/src|,../src'
au! BufEnter *.cpp let b:fswitchdst = 'hpp,h'
au! BufEnter *.cpp let b:fswitchlocs = 'reg:/src/include/,reg:|src|include/**|,../include'
au! BufEnter *.h let b:fswitchdst = 'cpp,c'
au! BufEnter *.h let b:fswitchlocs = 'reg:/include/src/,reg:/include.*/src|,../src'
au! BufEnter *.c let b:fswitchdst = 'h'
au! BufEnter *.c let b:fswitchlocs = 'reg:/src/include/,reg:|src|include/**|,../include'

nnoremap <leader>f :call FSwitch('%', '') <BAR> cd .<CR>

"Function to strip white space at the end of lines
function! s:StripWhiteSpaces()
    let pos = getpos(".")
    let reg = getreg('/')
    :%s/\s\+$//e
    call setpos('.',pos)
    call setreg('/',reg)
endfunction

"Strip white space on save
autocmd BufWritePre * StripWhiteSpace
command! -range=% StripWhiteSpaces :silent call <SID>StripWhiteSpaces()

"Set automatic insertion of line breaks for tex files
autocmd FileType tex setlocal formatoptions+=t

"Set the defautl compiler for c++ and tex files
autocmd FileType cpp compiler gcc
autocmd FileType tex let b:tex_flavor = 'pdflatex'
autocmd FileType tex compiler tex
autocmd FileType tex set wrap
autocmd FileType tex set linebreak

" Automatically open the quick fix window on make errors
autocmd QuickFixCmdPost [^l]* nested cwindow

au BufNewFile,BufRead *.cpp set syntax=cpp11

set wildignore=*.class
